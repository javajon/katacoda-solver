package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Verifications;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "request_advance_task", hidden = true, description = "Solve current task and on success advance current task number. Called by challenge framework, not by people.")
public class SubcommandRequestTaskAdvance implements Callable<Integer> {
    @Spec CommandSpec spec;

    @Parameters(index = "0", description = "Task number to advance from.")
    private int task;

    @Override
    public Integer call() {
        boolean advanced = new Verifications().requestTaskAdvance(task);
        out().printf("Verifications %s for task %d.%n", advanced ? "passed" : "failed", task);
        if (advanced) {
            out().printf("Advancing to task %d.%n", task);
        }

        return task;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
