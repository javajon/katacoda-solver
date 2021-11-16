package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Hints;
import com.katacoda.solver.models.Solutions;
import com.katacoda.solver.models.Verifications;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "view", description = "Reveal the verifications, hints, and solutions for a task.")
public class SubcommandView implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @CommandLine.Parameters(index = "0", defaultValue = "0", description = "Display hint for task.")
    private int task = 0;

    @CommandLine.Option(names = {"-v", "--verifications",}, required = false, description = "Show verifications for the task.")
    private boolean showVerifications = false;

    @CommandLine.Option(names = {"-h", "--hints",}, required = false, description = "Show hints for the task.")
    private boolean showHints = false;

    @CommandLine.Option(names = {"-s", "--solutions",}, required = false, description = "Show solutions for the task.")
    private boolean showSolutions = false;

    @Override
    public Integer call() {
        Verifications verifications = new Verifications();

        if (!verifications.exist(task)) {
            out().println(CommandLine.Help.Ansi.AUTO.string("@|bold,red " + "No information is available for requested task " + task + "|@"));
            return -1;
        }

        if (!showVerifications && !showHints && !showSolutions) {
            showVerifications = true;
            showHints = true;
            showSolutions = true;
        }

        dump();

        return 0;
    }

    private void dump() {
        if (showVerifications) {
            out().printf("---- Verifications for Task %d ----%n%n", task);
            out().println(CommandLine.Help.Ansi.AUTO.string("@|bold,yellow " + getVerifications(task) + "|@"));
        }

        if (showHints) {
            out().printf("%n---- Hints for Task %d ----%n%n", task);
            out().println(CommandLine.Help.Ansi.AUTO.string("@|bold,yellow " + getHints(task) + "|@"));
        }

        if (showSolutions) {
            out().printf("%n---- Solutions for Task %d ----%n%n", task);
            if (!new Solutions().exist(task)) {
                out().println(CommandLine.Help.Ansi.AUTO.string("@|yellow " + "Solutions not found for requested task " + task + "|@"));
            } else {
                out().println(CommandLine.Help.Ansi.AUTO.string("@|bold,yellow " + getSolutions(task) + "|@"));
            }
        }
    }

    private String getVerifications(int task) {
        String start = "function verify_task_" + task;
        String end = "function verify_task_" + (task + 1);
        return substringBetween(new Verifications().getSourceAsString(), start, end);
    }

    private String getHints(int task) {
        String start = "## Task " + task + ", Hint 1";
        String end = "## Task " + (task + 1) + ", Hint 1";
        return substringBetween(new Hints().getHintsAsString(), start, end);
    }

    private String getSolutions(int task) {
        String start = "function solve_task_" + task;
        String end = "function solve_task_" + (task + 1);
        return substringBetween(new Solutions().getSourceAsString(), start, end);
    }

    // Like org.apache.commons.lang3.StringUtils; (exclusive cutting for end tag)
    private String substringBetween(String source, String startToken, String endToken) {
        String result = source.substring(source.indexOf(startToken));
        int end = result.indexOf(endToken);
        if (end == -1) {
            end = result.length();
        }
        return result.substring(0, end).trim();
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
