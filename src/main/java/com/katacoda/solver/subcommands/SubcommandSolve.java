package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.Solutions;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.PrintWriter;
import java.util.concurrent.Callable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;


@Command(name = "solve", description = "Solve task number. Subsequent commands assume next task")
public class SubcommandSolve implements Callable<Integer> {

    @Spec CommandSpec spec;

    @Parameters(index = "0", description = "Task number to solve.")
    private int task;

    @Override
    public Integer call() {
        Configuration.setCurrentTask(task);
        return new Solutions().solve(out());
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
