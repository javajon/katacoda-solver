package com.katacoda.solver.models;

import org.jboss.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Hints {

    private static final Logger LOG = Logger.getLogger(Hints.class);

    private static final String HINT_HEADER_PATTERN = "## Task %d, Hint %d";
    private static final String HINT_END_PATTERN = "## Task";
    private static final Path LOCATION_IN_CHALLENGE = Path.of("/opt");
    private static final String HINTS_MARKDOWN = "hints.md";

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

        // Remove optional divider between sections
        lastLine = hintLines.size() - 1;
        if (hintLines.get(lastLine).trim().startsWith("---")) {
            hintLines.remove(lastLine);
        }

        return String.join("\n", hintLines).trim();
    }

    private InputStream getSource() throws FileNotFoundException {
        switch (Configuration.getContextType()) {
            case development:
                return getClass().getClassLoader().getResourceAsStream(HINTS_MARKDOWN);
            case authoring:
                return new FileInputStream(HINTS_MARKDOWN);
            case challenge:
                return new FileInputStream(new File(LOCATION_IN_CHALLENGE.toFile(), HINTS_MARKDOWN));
        }

        return InputStream.nullInputStream();
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
