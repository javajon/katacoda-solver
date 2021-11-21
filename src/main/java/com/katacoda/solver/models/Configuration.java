package com.katacoda.solver.models;

import org.jboss.logging.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;

/** Persist state of challenge across multiple calls to this application. */
public class Configuration {
    private static final Logger LOG = Logger.getLogger(Configuration.class);

    /**
     * Global environment settings so multiple instances of app can track current state.
     */
    private final static File SOLVER_SHARED_PROPERTIES = new File(System.getProperty("java.io.tmpdir"),
            "solver.properties");

    public static void resetCurrentTask() {
        setCurrentTask(1);
    }

    public static void setHintEnabled(boolean enabled)  {
        try {
            Properties props = load();
            props.setProperty("hints.enabled", Boolean.toString(enabled));
            store(props);
        } catch (ConfigurationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static boolean getHintsEnabled() {
        try {
            return Boolean.parseBoolean(load().getProperty("hints.enabled", "true"));
        } catch (ConfigurationException e) {
            LOG.error(e.getMessage(), e);
        }

        return false;
    }

    public static int getCurrentTask() {
        try {
            return Integer.parseInt(load().getProperty("task.current", "1"));
        } catch (ConfigurationException e) {
            LOG.error(e.getMessage(), e);
        }

        return -1;
    }

    public static void setCurrentTask(int task)  {
        Properties props;
        try {
            props = load();
            props.setProperty("task.current", Integer.toString(task));
            store(props);
        } catch (ConfigurationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void store(Properties prop) throws ConfigurationException {
        try (OutputStream output = new FileOutputStream(SOLVER_SHARED_PROPERTIES)) {
            prop.store(output, "Solver Properties");
        } catch (IOException e) {
            throw new ConfigurationException("Error storing configuration because: " + e.getMessage(), e);
        }
    }

    private static Properties load() throws ConfigurationException {
        // Ensure file exist, will not overwrite if already present
        try {
            SOLVER_SHARED_PROPERTIES.createNewFile();
        } catch (IOException e) {
            throw new ConfigurationException("Error loading configuration because: " + e.getMessage(), e);
        }

        Properties props = new Properties();
        try (InputStream input = new FileInputStream(SOLVER_SHARED_PROPERTIES)) {
            props.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Error loading configuration because: " + e.getMessage(), e);
        }

        return props;
    }

    public static boolean isChallengeComplete() {
        return getCurrentTask() <= 0;
    }

    public static boolean isChallengeComplete(int task) {
        return task <= 0;
    }

    public enum Environment {
        development,
        authoring,
        challenge
    }

    public static Environment getEnvironment() {
        if (Path.of("/usr", "local", "bin", "challenge.sh").toFile().exists()) {
            return Environment.challenge;
        }

        if (new File("gradlew").exists()) {
            return Environment.development;
        }

        return Environment.authoring;
    }

    public static class ConfigurationException extends Exception {
        public ConfigurationException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
