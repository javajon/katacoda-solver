package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.Solutions;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "all", description = "Solve all remaining tasks.")
public class SubcommandAll implements Callable<Integer> {

    @Spec CommandSpec spec;

    @Override
    public Integer call() {

        if (Configuration.getContextType() == Configuration.ContextType.authoring) {
            out().println("Command only valid in running challenge.");
            return -1;
        }

        int result = 0;
        while (result == 0 && !Configuration.isChallengeComplete() ) {
            result = new Solutions().solve(out());
        }

        return result;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
