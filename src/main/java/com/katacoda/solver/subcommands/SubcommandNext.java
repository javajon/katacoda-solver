package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Solutions;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "next", description = "Solve current task and on success advance current task number")
public class SubcommandNext implements Callable<Integer> {
    @Spec CommandSpec spec;

    @Override
    public Integer call() {
        return new Solutions().solve(out());
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
