package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.Solutions;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.PrintWriter;
import java.util.concurrent.Callable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "all", description = "Solve all remaining tasks")
public class SubcommandAll implements Callable<Integer> {

    @Spec CommandSpec spec;

    @Override
    public Integer call() {
        while (!Configuration.isChallengeComplete()) {
            new Solutions().solve(out());
        }

        return 0;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
