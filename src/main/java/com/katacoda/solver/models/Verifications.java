package com.katacoda.solver.models;

import org.jboss.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Verifications {

    private static final Logger LOG = Logger.getLogger(Verifications.class);

    private final static File SOURCER = new File(System.getProperty("java.io.tmpdir"), "verifications-sourcer.sh");
    private static final Path LOCATION_IN_CHALLENGE = Path.of("/usr/local/bin");
    public static final String VERIFICATIONS_SCRIPT = "verifications.sh";


    public int requestTaskAdvance(int task) {
        int status = verify(task);

        if (status == 0) {
            advanceTask();
        }

        return status;
    }

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

        if (!SOURCER.exists() || Configuration.getEnvironment() == Configuration.Environment.development) {
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

    private void advanceTask() {
        int task = Configuration.getCurrentTask();
        task++;

        Configuration.setCurrentTask(exist(task) ? task : 0);
    }

    public int verify(int task) {
        try {
            return executeShellFunction("verify_task_" + task);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return -1;
    }

    public boolean exist(int task) {
        try {
            return 0 == executeShellFunction("function_exists", "verify_task_" + task);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return false;
    }

    public List<String> getVerifications() {

        List<String> functions = Collections.emptyList();

        try {
            Stream<String> lines = new BufferedReader(new InputStreamReader(getSource(), StandardCharsets.UTF_8)).lines();
            functions = lines.collect(Collectors.toList());
            functions.replaceAll(String::trim);
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        }

        return functions;
    }
    
    public String getSourceAsString() {
        return String.join("\n", getVerifications());
    }

    
    private InputStream getSource() throws FileNotFoundException {
        switch (Configuration.getEnvironment()) {
            case development:
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(VERIFICATIONS_SCRIPT);
            case authoring:
                return new FileInputStream(VERIFICATIONS_SCRIPT);
            case challenge:
                return new FileInputStream(new File(LOCATION_IN_CHALLENGE.toFile(), VERIFICATIONS_SCRIPT));
        }

        return InputStream.nullInputStream();
    }
}
