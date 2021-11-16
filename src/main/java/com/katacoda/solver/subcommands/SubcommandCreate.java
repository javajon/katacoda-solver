package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import org.jboss.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Command(name = "create", commandListHeading = "Authoring", description = "Create any missing files that are needed by Solver. Will not overwrite. Creates only when in authoring environment.")
public class SubcommandCreate implements Callable<Integer> {

    private static final Logger LOG = Logger.getLogger(Configuration.class);
    private static final String LINUX_ARCHETYPE = "challenge-linux-solver.zip";

    @Spec
    CommandSpec spec;

    private static final File INDEX = new File("index.json");
    private static final File HINT = new File("hints.sh");

    // TODO
    private static final File VERIFY = new File("verify.sh");
    private static final File HINTS_MD = new File("assets/hints.md");
    private static final File VERIFICATIONS = new File("assets/verifications.sh");
    private static final File SOLUTIONS = new File("assets/solutions.sh");

    private enum Archetypes {scratch, linux, kubernetes}

    @Option(names = {"-a", "--archetype"}, required = true, description = "The general type of challenge to create.")
    private Archetypes archetype = Archetypes.kubernetes;

    @Option(names = {"-d", "--destination"}, description = "Path to destination where a new directory of the archetype will be created.")
    private String destination = ".";

    @Option(names = {"-f", "--force"}, required = false, description = "Force overwrite of existing files with confirmation.", defaultValue = "false")
    private boolean force = false;

    @Override
    public Integer call() {
        Path target = Path.of(destination);
        if (!target.toFile().isDirectory()) {
            out(String.format("Destination path %s to create the archetype is not a directory.", target.toAbsolutePath()));
            return 1;
        }

        switch (archetype) {

            case scratch:
                out("Archetype is not functional yet. See roadmap.");
                // createScratch();
                return 1;

            case linux:
                createLinux(target);
                out(String.format("A new project %s has been created at %s", LINUX_ARCHETYPE, destination));
                out("Copy a version of the solver tool into the " + LINUX_ARCHETYPE + "/assets directory.");
                // TODO this will be replaced with a command to get latest from public repo release page.
                break;

            case kubernetes:
                out("Archetype is not functional yet. See roadmap.");
                return 1;
        }

        return 0;
    }

    private void createScratch() {
        String challengeSrcDir = "";
        File indexJson = new File(challengeSrcDir, INDEX.toString());

        out("Create command is not functional yet. See roadmap.");

        // Ensure in correct directory
        if (!indexJson.exists()) {
            out(String.format("The file %s was not found. Run create in the directory where the challenge index.json file exists, or define --path.%n", indexJson.getAbsolutePath()));
        }

        // Check for hint.sh
        create(HINT);
    }

    private void createLinux(Path destination) {
        try (ZipInputStream zis = new ZipInputStream(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream(LINUX_ARCHETYPE)))) {
            expandContents(destination.toFile(), zis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void expandContents(File destDir, ZipInputStream zis) throws IOException {
        byte[] buffer = new byte[1024];

        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            zipEntry = zis.getNextEntry();
        }
    }


    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void create(File artifact) {
        boolean create = !artifact.exists();
        if (artifact.exists()) {
            if (force) {
                String s = System.console().readLine(String.format("Overwrite existing file %s? y/n: ", artifact));
                create = Boolean.parseBoolean(s) || "y".equalsIgnoreCase(s);
            }
        }
        if (create) {
            try {
                artifact.createNewFile();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void out(String message) {
        spec.commandLine().getOut().println(message);
    }
}
