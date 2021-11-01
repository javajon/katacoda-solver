package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Verifications;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "challenge_advance_task", hidden = true, description = "Solve current task and on success advance current task number. Called by challenge framework, not by people.")
public class SubcommandRequestTaskAdvance implements Callable<Integer> {
    @Spec CommandSpec spec;

    @Parameters(index = "0", description = "Task number to advance from.")
    private int task;

    @Override
    public Integer call() {
        int result = new Verifications().requestTaskAdvance(task);
        out().printf("Verifications %s for task %d.%n", result == 0 ? "passed" : "failed", task);
        if (result > 0) {
            out().printf("Advancing to task %d.%n", task);
        }

        return result;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
