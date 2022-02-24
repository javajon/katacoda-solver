package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.VersionProvider;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Command(name = "create", commandListHeading = "Authoring", description = "Create a Challenge project from the given archetype when in authoring context.")
public class SubcommandCreate implements Callable<Integer> {

    private static final Logger LOG = Logger.getLogger(Configuration.class);
    private static final String LINUX_ARCHETYPE = "challenge-linux-solver.zip";

    private static final File INDEX = new File("index.json");
    private static final File HINT = new File("hints.sh");

    @Spec
    CommandSpec spec;

    @Option(names = {"-a", "--archetype"}, required = true, defaultValue = "linux", showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "The type of challenge to create. Default linux.")
    private Archetypes archetype = Archetypes.kubernetes;

    @Option(names = {"-n", "--name"}, required = false, description = "Optional name of directory for new challenge skaffold files. Default challenge-<archetype>.", defaultValue = "")
    private String name = "";

    @Option(names = {"-f", "--force"}, required = false, description = "Force overwrite of existing files with confirmation. Default false.", defaultValue = "false")
    private boolean force = false;

    @Option(names = {"-t", "--target"}, required = false, description = "Targetted path to directory for new challenge skaffold files. Missing directories will be created. Default current directory.", defaultValue = ".")
    private String target = ".";

    private enum Archetypes {basic, linux, kubernetes}

    /**
     * Directory where archetype subdirectory is created. Typically, working directory, but unit tests can override.
     */
    private Path targetPath = Path.of(target);


    @Override
    public Integer call() {

        targetPath = Path.of(target);
        if (!targetPath.toFile().exists()) {
            targetPath.toFile().mkdir();
        }

        if (Configuration.getContextType() == Configuration.ContextType.challenge) {
            out("Command only valid during challenge authoring.");
            return 1;
        }

        if (name.isEmpty()) {
            name = "challenge-" + archetype.name();
        }

        File destination = new File(targetPath.toFile(), name);
        if (!force && destination.exists()) {
            out(String.format("The archetype was not created because the destination %s exists with files in it. The destination remains untouched. Use --force to override.", destination));
            return 2;
        }

        switch (archetype) {
            case basic:
                // createBasic(targetPath, name);
                out("Archetype `basic` is not functional yet. See roadmap.");
                return 3;

            case linux:
                createLinux(targetPath, name);
                break;

            case kubernetes:
                // createKubernetes(targetPath, name);
                out("Archetype `kubernetes` is not functional yet. See roadmap.");
                return 3;
        }

        Path projectPath = Path.of(targetPath.toString(), name);
        out(String.format("A new %s archetype project has been created at %s.", archetype, projectPath));

        return syncSolverVersionReference(projectPath);
    }

    /**
     * Ensure challenge uses matching version of solver
     * In init-background.sh set the version in SOLVER_VERSION=x.y.z
     *
     * @param projectDirName
     * @return
     */
    private int syncSolverVersionReference(Path projectPath) {
        String versionMessage = new VersionProvider().getVersion()[0];
        String version = versionMessage.split("\\s+")[2]; // extract version from "Solver version x.y.z"

        Path scriptFile = projectPath.resolve("init-background.sh");
        try (Stream<String> lines = Files.lines(scriptFile)) {
            List<String> replaced = lines
                    .map(line -> swap(line, version))
                    .collect(Collectors.toList());

            Files.write(scriptFile, replaced);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return -1;
        }

        return 0;
    }

    private static String swap(String line, String version) {
        if (line.startsWith("SOLVER_VERSION=")) {
            return "SOLVER_VERSION=" + version;
        }
        return line;
    }

    /**
     * Bare minimum skeleton.
     */
    private void createBasic() {
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

    private String createLinux(Path destination, String name) {
        try (ZipInputStream zis = new ZipInputStream(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream(LINUX_ARCHETYPE)))) {
            return expandContents(destination.toFile(), zis, name);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return "";
    }

    private String expandContents(File destDir, ZipInputStream zis, String name) throws IOException {
        byte[] buffer = new byte[1024];

        ZipEntry zipEntry = zis.getNextEntry();
        String projectDirName = zipEntry == null ? "" : Path.of(zipEntry.getName()).getParent().toString();
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

        new File(destDir, projectDirName).renameTo(new File(destDir, name));

        return projectDirName;
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
