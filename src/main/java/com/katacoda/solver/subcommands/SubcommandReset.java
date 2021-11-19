package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "reset", description = "Clear task tracker so next task is assumed to be 1")
public class SubcommandReset implements Callable<Integer> {
    @Spec CommandSpec spec;

    @Override
    public Integer call() {

        if (Configuration.getEnvironment() == Configuration.Environment.authoring) {
            out().println("Command only valid in running challenge.");
            return -1;
        }

        out().printf(String.format("The current task has been set back to the first step (%d).", Configuration.getCurrentTask()));

        return 0;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
