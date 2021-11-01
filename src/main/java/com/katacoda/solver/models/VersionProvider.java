package com.katacoda.solver.models;

import org.jboss.logging.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionProvider implements CommandLine.IVersionProvider {

    private static final Logger LOG = Logger.getLogger(VersionProvider.class);

    @Override
    public String[] getVersion() {

        Properties p = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            p.load(is);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        String version = p.getProperty("project.version", "unknown");
        return new String[]{"Solver version " + version};
    }
}
