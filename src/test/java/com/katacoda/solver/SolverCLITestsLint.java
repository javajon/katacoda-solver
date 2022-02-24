package com.katacoda.solver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.mapping.ScenarioIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolverCLITestsLint {

    private SolverTopCommand cli;

    private StringWriter sw;

    private StringWriter swErr;

    private CommandLine cmd;

    @BeforeEach
    void setUp() {
        cli = new SolverTopCommand();
        cmd = new CommandLine(cli);

        sw = new StringWriter();
        swErr = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        cmd.setErr(new PrintWriter(swErr));

        // For environment in authoring
        System.setProperty(Configuration.SOLVER_CONTEXT, Configuration.ContextType.authoring.name());
    }

    @AfterEach
    void tearDown() {
        // For environment in authoring
        System.setProperty(Configuration.SOLVER_CONTEXT, "");
    }


    @Test
    public void checkDevEnvironment() {
        // For environment in authoring
        System.setProperty(Configuration.SOLVER_CONTEXT, "");

        int exitCode = cmd.execute("lint");
        assertEquals(1, exitCode);
    }

    @Test
    public void checkAuthoringEnvironment() {

        int exitCode = cmd.execute("lint");
        assertEquals(1, exitCode);
    }

    @Test
    public void checkAuthoringDetails() throws IOException {
        int exitCode;

        String testingDirectory = scrubTestArtifacts();

        // Create new linux skaffold
        exitCode = cmd.execute("create", "--archetype", "linux", "--target", testingDirectory);
        assertEquals(0, exitCode);

        exitCode = cmd.execute("lint", "--target", new File(testingDirectory, "challenge-linux").toString());
        assertEquals(1, exitCode);

        // TODO other tests
        //Files.delete(Path.of(testingDirectory, "challenge-linux/index.json"));
    }

    private String scrubTestArtifacts() throws IOException {
        // Scrub testing directory in the build location
        String testingDirectory = "build/test-challenges";
        if (new File(testingDirectory).exists()) {
            Files.walk(Path.of(".", testingDirectory))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        return testingDirectory;
    }
}
