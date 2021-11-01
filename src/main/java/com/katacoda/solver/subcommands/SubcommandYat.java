package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "yat", description = "Get the next task to solve. Where are you at (y'at)?")
public class SubcommandYat implements Callable<Integer> {
    @Spec CommandSpec spec;

    @Option(names = {"-q", "--quiet"}, required = false, description = "Just provide the current next task number to solve.")
    private boolean quiet;

    @Override
    public Integer call() {
        int task = Configuration.getCurrentTask();

        if (quiet) {
            out().println(task);
        } else {
            if (task > 0) {
                out().printf("Next task to solve is %d.%n", task);
            } else {
                out().println("All tasks have been verified and completed.");
            }
        }

        return 0;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
