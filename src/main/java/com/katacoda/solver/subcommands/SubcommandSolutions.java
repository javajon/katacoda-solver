package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.CryptoUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.text.WordUtils;
import io.quarkus.logging.Log;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "solutions", aliases = {"sol"}, description = "Install solutions for testing. Requires authoring passcode.")
public class SubcommandSolutions implements Callable<Integer> {

    @Spec CommandSpec spec;

    private static final String SOLUTIONS_SCRIPT = "solutions.sh";
    private static final String SOLUTIONS_KEY = SOLUTIONS_SCRIPT + ".key";
    private static final Path SOLUTIONS_ENCRYPTED = Paths.get("/opt", SOLUTIONS_SCRIPT + ".enc");
    private static final Path SOLUTIONS_DECRYPTED = Paths.get("/usr", "local", "bin", SOLUTIONS_SCRIPT);

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @Option(names = {"-e", "--encrypt"})
        boolean encrypt;
        @Option(names = {"-d", "--decrypt"})
        boolean decrypt;
    }

    @CommandLine.Option(names = {"-p", "--path",}, required = false, description = "The path of the source code challenge directory. Current directory if no path provided.")
    private Path challange_src = FileSystems.getDefault().getPath(".");

    @Override
    public Integer call() {
        try {
            doSolutions();
        } catch (SolutionsException e) {
            Log.error(e.getMessage(), e);
            String message = String.format("Could not %s because: %s", exclusive.encrypt ? "encrypt" : "decrypt", e.getCause());
            out().println(CommandLine.Help.Ansi.AUTO.string("@|bold,red " + message + "|@"));
            return 1;
        }

        return 0;
    }

    private void doSolutions() throws SolutionsException {
        Log.info("e: " + exclusive.encrypt + " d: " + exclusive.decrypt);
        if (exclusive.encrypt) {
            if (Configuration.getEnvironment() == Configuration.Environment.authoring) {
                encrypt();
            }
            else {
                out().println(CommandLine.Help.Ansi.AUTO.string("@|bold,red " + "The solutions.sh file can only be encrypted when authoring the challenge source." + "|@"));
            }
        }
        if (exclusive.decrypt) {
            decrypt();
        }
    }

    private void decrypt() throws SolutionsException {
        decrypt(getKey());
    }

    private void decrypt(String key) throws SolutionsException {
        try {
            Files.createDirectories(SOLUTIONS_DECRYPTED.getParent());

            CryptoUtils.decrypt(key, SOLUTIONS_ENCRYPTED.toFile(), SOLUTIONS_DECRYPTED.toFile());
        } catch (CryptoUtils.CryptoException | IOException e) {
            throw new SolutionsException("Could not decrypt solutions because: " + e.getCause(), e);
        }

        makeSolutionsScriptExecutable();
    }

    private void makeSolutionsScriptExecutable() throws SolutionsException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        try {
            Files.setPosixFilePermissions(SOLUTIONS_DECRYPTED, perms);
        } catch (IOException e) {
            throw new SolutionsException("Could not change file permissions for " + SOLUTIONS_DECRYPTED + " because: " + e.getCause(), e);
        }
    }

    private String getKey() throws SolutionsException {
        String key;
        try {
            key = Files.readString(FileSystems.getDefault().getPath(SOLUTIONS_KEY));
        } catch (IOException e) {
            throw new SolutionsException("Could not access expected key file " + SOLUTIONS_KEY + " because: " + e.getCause(), e);
        }
        return key;
    }

    private void encrypt() throws SolutionsException {
        String key = CryptoUtils.getRandomKey();
        String keyHex = Hex.encodeHexString(key.getBytes());

        encrypt(key);
        recordKey(key, keyHex);

        addSolutionsEncToCopiedAssets();

        String message =
                String.format("The %1$s has been encrypted with the AES algorithm using the salt passcode %1$s. The passcode is written to the file %1$s.md. The encoded file %1$s.enc and placed in the challenge's assets folder. The encoded file has been added as an assets to be copied to the scenario. To use the %1$s file in the scenario use `solver solutions --decrypt`", SOLUTIONS_SCRIPT, keyHex);
        out().println(WordUtils.wrap(message, 80));
    }

    private void addSolutionsEncToCopiedAssets() {
        // TODO add to index.json
    }

    private void encrypt(String key) throws SolutionsException {
        File output = new File(new File(challange_src.toString(), "assets"), SOLUTIONS_SCRIPT + ".enc");

        try {
            Files.createDirectories(output.getParentFile().toPath());
            CryptoUtils.encrypt(key, new File(SOLUTIONS_SCRIPT), output);
        } catch (CryptoUtils.CryptoException | IOException e) {
            throw new SolutionsException("Could not encrypt solutions.sh file because: " + e.getCause(), e);
        }
    }

    private void recordKey(String key, String keyHex) throws SolutionsException {
        try {
            Files.writeString(FileSystems.getDefault().getPath(challange_src.toString(), SOLUTIONS_SCRIPT + ".key"), key);
            Files.writeString(FileSystems.getDefault().getPath(challange_src.toString(),SOLUTIONS_SCRIPT + ".md"), String.format("The solutions.sh.enc file has been encrpyted with the salt passcode: `%s`. Use this to decrypt manually `openssl enc -aes-128-ecb -d -in solutions.sh.enc -out 1.sh -K %s`.", keyHex, keyHex));
        } catch (IOException e) {
            throw new SolutionsException("Could not record key file because: " + e.getCause(), e);
        }
    }

    public static class SolutionsException extends Exception {

        public SolutionsException() {
        }

        public SolutionsException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
