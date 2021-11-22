package com.katacoda.solver.models;

import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Solutions {

    private static final Logger LOG = Logger.getLogger(Solutions.class);

    private final static File SOURCER = new File(System.getProperty("java.io.tmpdir"), "solutions-sourcer.sh");
    private static final Path LOCATION_IN_CHALLENGE = Path.of("/usr/local/bin");
    private static final String SOLUTIONS_SCRIPT = "solutions.sh";


    public int executeShellFunction(String... functionAndParams) throws IOException {

        File functionSourcingScript = getFunctionSourcingScript();

        List<String> processParams = new ArrayList<>(List.of("bash", functionSourcingScript.toString()));
        processParams.addAll(Arrays.asList(functionAndParams));

        ProcessBuilder pb = new ProcessBuilder(processParams);
        pb.inheritIO();
        Process process = pb.start();
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            return -1;
        }
    }

    private File getFunctionSourcingScript() {

        if (!SOURCER.exists() || Configuration.getContextType() == Configuration.ContextType.development) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(SOURCER)))) {
                writer.println("#!/bin/bash");
                writer.println("# Inserted functions");
                writer.println(getSourceAsString());
                writer.println("# Inserted functions");
                writer.println("function function_exists() {");
                writer.println(" [ $(type -t \"$1\")\"\" == 'function' ]"); // https://stackoverflow.com/a/9002012/3236525
                writer.println("}");
                writer.println("\"$@\""); // Call functions above based on passed in parameter(s)
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        return SOURCER;
    }

    public boolean exist(int task) {
        try {
            getSource();
            return solutionFunctionExists(task);
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        }

        return false;
    }

    private int solveFunction(int task) {
        try {
            return executeShellFunction("solve_task_" + task);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return -1;
    }


    private boolean solutionFunctionExists(int task) {
        try {
            return 0 == executeShellFunction("function_exists", "solve_task_" + task);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return false;
    }

    public int solve(PrintWriter out) {
        return solve(Configuration.getCurrentTask(), out);
    }

    private int solve(int task, PrintWriter out) {
        if (Configuration.isChallengeComplete(task)) {
            return 0;
        }

        // Solve task
        if (exist(task)) {
            solveFunction(task);
        } else {
            String message = String.format("Cannot solve the task. The solutions.sh testing functions script was not found or the solve_task_%d function was not found. O'Reilly team members can run `solver solution --decrypt <key>` to enable the solutions script. The key can be referenced in the source code's solutions.sh.md file.", task);
            out.println(Ansi.AUTO.string("@|bold,yellow " + message + "|@"));
            return -1;
        }

        pause();

        // Verify solution until passed
        int result;
        while ((result = new Verifications().verify(task)) != 0) {
            out.println(Ansi.AUTO.string("@|bold,yellow " + "Verifications not passing yet for task " + task + ", trying again." + "|@"));
            pause();
        }

        out.println(Ansi.AUTO.string("@|bold,green " + "Verifications passed for task " + task + "." + "|@"));

        if (Configuration.isChallengeComplete()) {
            out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,green " + "All tasks have been solved and the challenge is complete." + "|@"));
        }

        return result;
    }

    public List<String> getSolutions() {

        List<String> hintsMarkdown = Collections.emptyList();

        try {
            InputStream source = getSource();
            if (! source.equals(InputStream.nullInputStream())) {
                Stream<String> lines = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8)).lines();
                hintsMarkdown = lines.collect(Collectors.toList());
                hintsMarkdown.replaceAll(String::trim);
            }
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        }

        return hintsMarkdown;
    }

    public String getSourceAsString() {
        return String.join("\n", getSolutions());
    }

    private InputStream getSource() throws FileNotFoundException {
        switch (Configuration.getContextType()) {
            case development:
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(SOLUTIONS_SCRIPT);
            case authoring:
                File solutionsAuthoring = new File(SOLUTIONS_SCRIPT);
                if (solutionsAuthoring.exists()) {
                    return new FileInputStream(solutionsAuthoring);
                }
            case challenge:
                File solutionsChallenge = new File(LOCATION_IN_CHALLENGE.toFile(), SOLUTIONS_SCRIPT);
                if (solutionsChallenge.exists()) {
                    return new FileInputStream(solutionsChallenge);
                }
        }

        return InputStream.nullInputStream();
    }


    private static void pause() {
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
