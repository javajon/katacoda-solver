package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.Solutions;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.PrintWriter;
import java.util.concurrent.Callable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "until", description = "Solve all remaining tasks until reaching given task number")
public class SubcommandUntil implements Callable<Integer> {

    @Spec CommandSpec spec;

    @Parameters(index = "0", description = "Solve all task from current task to this given task.")
    private int untilTask;

    @Override
    public Integer call() {
        while (!Configuration.isChallengeComplete() && Configuration.getCurrentTask() < untilTask) {
            new Solutions().solve(out());
        }

        if (!Configuration.isChallengeComplete()) {
            out().printf("Stopped at task %d.%n", Configuration.getCurrentTask());
        }

        return 0;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
