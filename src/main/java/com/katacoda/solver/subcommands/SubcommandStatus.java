package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "status", description = "Get the next task to solve.")
public class SubcommandStatus implements Callable<Integer> {
    @Spec CommandSpec spec;

    @Option(names = {"-q", "--quiet"}, required = false, description = "Just provide the current next task number to solve.")
    private boolean quiet;

    @Override
    public Integer call() {
        if (Configuration.getEnvironment() == Configuration.Environment.authoring) {
            out().println("Command only valid in running challenge.");
            return -1;
        }

        int task = Configuration.getCurrentTask();

        if (quiet) {
            out().println(task);
        } else {
            if (Configuration.isChallengeComplete()) {
                out().println("All tasks have been verified and completed.");
            } else {
                out().printf("Challenge is incomplete and next task to solve is %d.%n", task);
            }
        }

        return 0;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
