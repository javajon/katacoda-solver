package com.katacoda.solver.subcommands;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.CryptoUtils;
import com.katacoda.solver.models.Hints;
import net.minidev.json.JSONArray;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "solutions", aliases = {"sol"}, commandListHeading = "Authoring", description = "Install solutions for testing. Requires authoring passcode.")
public class SubcommandSolutions implements Callable<Integer> {

    private static final Logger LOG = Logger.getLogger(Hints.class);

    @Spec
    CommandSpec spec;

    private static final String SOLUTIONS_SCRIPT = "solutions.sh";
    private static final String SOLUTIONS_SCRIPT_ENC = SOLUTIONS_SCRIPT + ".enc";
    private static final Object SOLUTIONS_SCRIPT_ENC_TARGET = "/opt";
    private static final String INDEX_NAME = "index.json";
    private static final Path INDEX = Paths.get(INDEX_NAME);

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @Option(names = {"-e", "--encrypt"})
        boolean encrypt;
        @Option(names = {"-d", "--decrypt"}, description = "Hex key used to decrypt, found in solutions.sh.md")
        String key = "";
    }

    private final String instructions = String.join(
            " ",
            "# Encryption Instructions%n%n",
            "The assets/%1$s has been encrypted to the `assets/%1$s.enc` file",
            "with the passcode: `%2$s`. The passcode is written to `assets/%1$s.md`",
            "and should be stored in version control. This passcode is for authors and",
            "other testers and should never be revealed to learners. Never copy",
            "`%1$s.md` or the key as an asset to the Challenge. When in the Challenge,",
            "as an author or tester, refer to this key to install the solutions file with",
            "`solver solutions --decrypt <key>`. Manual decryption can be done with",
            "`openssl enc -aes-128-ecb -d -in /opt/%1$s.enc -out /usr/local/bin/%1$s -K $(echo -n %2$s | hexdump -ve '1/1 \"%%.2x\"')`.",
            "Once the `/usr/local/bin/%1$s` script is present the solver testing commands",
            "like `next`, `all`, `until`, and `solve` will help solve each Challenge task.\n"
    );

    @Override
    public Integer call() {

        try {
            return doSolutions();
        } catch (SolutionsException e) {
            String message = String.format("Could not %s because: %s", exclusive.encrypt ? "encrypt" : "decrypt", e.getMessage());
            LOG.error(message, e);
            out(CommandLine.Help.Ansi.AUTO.string("@|bold,red " + message + "|@"));
            return 1;
        }
    }

    private int doSolutions() throws SolutionsException {
        LOG.info("encrypt: " + exclusive.encrypt + ", decrypt key: " + exclusive.key);

        if (exclusive.encrypt) {
            if (Configuration.getEnvironment() == Configuration.Environment.challenge) {
                throw new SolutionsException("The solutions.sh file cannot be encrypted within a running challenge.");
            }
            encrypt();
        } else if (!exclusive.key.isEmpty()) {
            return decrypt(exclusive.key);
        }

        return 0;
    }

    private void encrypt() throws SolutionsException {
        String key = CryptoUtils.getRandomKey();

        encrypt(key);
        recordKey(key);
        checkSolutionsEncInIndexCopiedAssets();
        showInstructions(key);
    }

    private int decrypt(String key) throws SolutionsException {

//        try {
//            Files.createDirectories(decryptLocation().getParent());
//        } catch (IOException e) {
//            throw new SolutionsException("Could not decrypt solutions because: " + e.getMessage(), e);
//        }

        try (InputStream input = decryptSource(); OutputStream output = decryptTarget()) {
            CryptoUtils.decrypt(key, input, output);
        } catch (CryptoUtils.CryptoException e) {
            out(CommandLine.Help.Ansi.AUTO.string("@|bold,red " + "Invalid decryption key: " + key + "|@"));
            LOG.debug(e.getMessage(), e);
            return 1;
        } catch (IOException e) {
            throw new SolutionsException(e.getMessage(), e);
        }

        makeSolutionsScriptExecutable();
        out("Solutions now available for commands such as `next`, `all`, `until`, and `solve`.");

        return 0;
    }

    private void makeSolutionsScriptExecutable() throws SolutionsException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        try {
            Files.setPosixFilePermissions(decryptLocation(), perms);
        } catch (IOException e) {
            throw new SolutionsException("Could not change file permissions for " + SOLUTIONS_SCRIPT + " because: " + e.getMessage(), e);
        }
    }

    private void showInstructions(String keyHex) {
        String message = String.format(instructions, SOLUTIONS_SCRIPT, keyHex);
        out(message);
    }

    private void checkSolutionsEncInIndexCopiedAssets() throws SolutionsException {
        DocumentContext jsonContext = JsonPath.parse(getIndex());
        Filter filter = Filter.filter(Criteria.where("file").eq("solutions.sh.enc"));
        JSONArray array = jsonContext.read("$['details']['assets'][*][?]", filter);

        boolean copyEncryptedSolution = false;
        if (!array.isEmpty()) {
            Map<String, String> copyElement = (Map<String, String>) array.get(0);
            String file = copyElement.get("file");
            String target = copyElement.get("target");

            copyEncryptedSolution = file.equals(SOLUTIONS_SCRIPT_ENC) && target.equals(SOLUTIONS_SCRIPT_ENC_TARGET);
        }

        String message = String.format("In %s the asset copy of %s to target %s is %s.",
                INDEX_NAME,
                SOLUTIONS_SCRIPT_ENC,
                SOLUTIONS_SCRIPT_ENC_TARGET,
                copyEncryptedSolution ? "correct" : "incorrect");
        out(message);
        if (!copyEncryptedSolution) {
            message += " Be sure to add to the assets section {\"file\": \"solutions.sh.enc\", \"target\": \"/opt\"} in " + INDEX_NAME;
            throw new SolutionsException(message);
        }
    }

    private Path encryptLocation() {
        switch (Configuration.getEnvironment()) {
            case development:
                return Path.of(System.getProperty("java.io.tmpdir"), SOLUTIONS_SCRIPT_ENC);
            case authoring:
                return Path.of("assets", SOLUTIONS_SCRIPT_ENC);
            case challenge:
                return Path.of("/opt", SOLUTIONS_SCRIPT_ENC);
        }

        return Path.of("");
    }

    private Path decryptLocation() {
        switch (Configuration.getEnvironment()) {
            case development:
                return Path.of(System.getProperty("java.io.tmpdir"), SOLUTIONS_SCRIPT);
            case authoring:
                return Path.of("assets", SOLUTIONS_SCRIPT);
            case challenge:
                return Path.of("/usr", "local", "bin", SOLUTIONS_SCRIPT);
        }

        return Path.of("");
    }

    private InputStream encryptSource() throws SolutionsException {
        if (Configuration.getEnvironment() == Configuration.Environment.development) {
            return getClass().getClassLoader().getResourceAsStream(SOLUTIONS_SCRIPT);
        }

        try {
            return new FileInputStream(decryptLocation().toFile());
        } catch (FileNotFoundException e) {
            throw new SolutionsException(e.getMessage(), e);
        }
    }


    private OutputStream encryptTarget() throws SolutionsException {
        try {
            return new FileOutputStream(encryptLocation().toFile());
        } catch (FileNotFoundException e) {
            throw new SolutionsException(e.getMessage(), e);
        }
    }

    private InputStream decryptSource() throws SolutionsException {
        try {
            return new FileInputStream(encryptLocation().toFile());
        } catch (FileNotFoundException e) {
            throw new SolutionsException(e.getMessage(), e);
        }
    }

    private OutputStream decryptTarget() throws SolutionsException {
        try {
            return new FileOutputStream(decryptLocation().toFile());
        } catch (FileNotFoundException e) {
            throw new SolutionsException(e.getMessage(), e);
        }
    }

    private InputStream getIndex() throws SolutionsException {
        switch (Configuration.getEnvironment()) {
            case development:
                return getClass().getClassLoader().getResourceAsStream(INDEX_NAME);
            case authoring:
                try {
                    return new FileInputStream(INDEX.toFile());
                } catch (FileNotFoundException e) {
                    throw new SolutionsException("Could read index because: " + e.getMessage(), e);
                }
            case challenge:
                // does present in live challenge
                break;
        }

        return InputStream.nullInputStream();
    }


    private void encrypt(String key) throws SolutionsException {
        try (InputStream input = encryptSource(); OutputStream output = encryptTarget()) {
            CryptoUtils.encrypt(key, input, output);
        } catch (CryptoUtils.CryptoException | IOException e) {
            throw new SolutionsException("Could not encrypt solutions.sh file because: " + e.getMessage(), e);
        }
    }

    private void recordKey(String key) throws SolutionsException {
        try {
            String message = String.format(instructions, SOLUTIONS_SCRIPT, key);
            Files.writeString(Paths.get(encryptLocation().getParent().toString(), SOLUTIONS_SCRIPT + ".md"), message);
        } catch (IOException e) {
            throw new SolutionsException("Could not record key file because: " + e.getMessage(), e);
        }
    }

    private void out(String message) {
        spec.commandLine().getOut().println(message);
    }

    public static class SolutionsException extends Exception {
        public SolutionsException(String message) {
            super(message);
        }
        public SolutionsException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
