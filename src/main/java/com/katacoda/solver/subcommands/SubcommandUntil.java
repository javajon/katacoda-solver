package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.Solutions;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "until", description = "Solve all tasks from current task until reaching given task number.")
public class SubcommandUntil implements Callable<Integer> {

    @Spec CommandSpec spec;

    @Parameters(index = "0", description = "Solve all task from current task to this given task.")
    private int untilTask;

    @Override
    public Integer call() {

        if (Configuration.getContextType() == Configuration.ContextType.authoring) {
            out().println("Command only valid in running challenge.");
            return -1;
        }

        int result = 0;
        while (result == 0 && !Configuration.isChallengeComplete() && Configuration.getCurrentTask() < untilTask) {
            result = new Solutions().solve(out());
        }

        if (!Configuration.isChallengeComplete()) {
            out().printf("Stopped at task %d.%n", Configuration.getCurrentTask());
        }

        return result;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
