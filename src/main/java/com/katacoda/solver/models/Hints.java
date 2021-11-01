package com.katacoda.solver.models;

import org.jboss.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Hints {

    private static final Logger LOG = Logger.getLogger(Hints.class);

    private static final String HINT_HEADER_PATTERN = "## Task %d, Hint %d";
    private static final String HINT_END_PATTERN = "## Task";

    public void enable(boolean enabled) {
        Configuration.setHintEnabled(enabled);
    }

    public String getHint(int task, int hint) {
        String search = String.format(HINT_HEADER_PATTERN, task, hint);

        List<String> hintLines = getHints();
        int fromIndex = hintLines.indexOf(search);
        if (fromIndex == -1) {
            return "";
        }
        fromIndex++;

        String endLine = hintLines.stream().skip(fromIndex).filter(line -> line.startsWith(HINT_END_PATTERN)).findFirst().orElse("");
        int toIndex = hintLines.indexOf(endLine);
        if (toIndex == -1) {
            return "";
        }

        hintLines = hintLines.subList(fromIndex, toIndex);
        if (hintLines.get(0).isEmpty()) {
            hintLines.remove(0);
        }
        int lastLine = hintLines.size() - 1;
        if (hintLines.get(lastLine).isEmpty()) {
            hintLines.remove(lastLine);
        }

        return String.join("\n", hintLines).trim();
    }

    private InputStream getSource() throws FileNotFoundException {
        switch (Configuration.getEnvironment()) {
            case development:
                return getClass().getClassLoader().getResourceAsStream("hints.md");
            case authoring:
                return new FileInputStream("hints.md");
            case challenge:
                return new FileInputStream("/opt/hints.md");
        }

        return new FileInputStream("");
    }

    public List<String> getHints() {

        List<String> hintsMarkdown = Collections.emptyList();

        try {
            Stream<String> lines = new BufferedReader(new InputStreamReader(getSource(), StandardCharsets.UTF_8)).lines();
            hintsMarkdown = lines.collect(Collectors.toList());
            hintsMarkdown.replaceAll(String::trim);
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        }

        return hintsMarkdown;
    }

    public String getHintsAsString() {
        return String.join("\n", getHints());
    }
}
