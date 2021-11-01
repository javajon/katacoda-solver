package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import org.jboss.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "create", description = "Create any missing files that are needed by Solver. Will not overwrite.")
public class SubcommandCreate implements Callable<Integer> {

    private static final Logger LOG = Logger.getLogger(Configuration.class);

    @Spec CommandSpec spec;

    private static final File INDEX = new File("index.json");
    private static final File HINT = new File("hints.sh");

    // TODO
    private static final File VERIFY = new File("verify.sh");
    private static final File HINTS_MD = new File("assets/hints.md");
    private static final File VERIFICATIONS = new File("assets/verifications.sh");
    private static final File SOLUTIONS = new File("assets/solutions.sh");

    private enum Archetypes { ubuntu, kubernetes }

    @Option(names = {"-a", "--archetype"}, description = "The general type of challenge to create.")
    private Archetypes archetype = Archetypes.kubernetes;

    @Option(names = {"-f", "--force"}, description = "Force overwrite of existing files with confirmation.", defaultValue = "false")
    private boolean force = false;

    @Override
    public Integer call() {
        String challengeSrcDir = "";
        File indexJson = new File(challengeSrcDir, INDEX.toString());

        out().printf("Create command is not functional yet. See roadmap.");

        // Ensure in correct directory
        if (!indexJson.exists()) {
            out().printf("The file %s was not found. Run create in the directory where the challenge index.json file exists, or define --path.%n", indexJson.getAbsolutePath());
        }

        // Check for hint.sh
        create(HINT);

        return 0;
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

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
