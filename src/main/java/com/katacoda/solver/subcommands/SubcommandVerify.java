package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Verifications;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Option;

@Command(name = "verify", description = "Verify task number is complete")
public class SubcommandVerify implements Callable<Integer> {

    @Spec CommandSpec spec;

    @Parameters(index = "0", defaultValue = "0", description = "Task number to verify.")
    private int task = 0;

    @Option(names = {"-q", "--quiet"}, required = false, description = "Output formatted for challenge interface.")
    private boolean quiet = false;

    @Override
    public Integer call() {
        int result = new Verifications().verify(task);

        if (!quiet) {
            if (result == 0) {
                out().println(Ansi.AUTO.string("@|bold,green " + "Verifications passed for task " + task + "." + "|@"));
            } else {
                out().println(Ansi.AUTO.string("@|bold,yellow " + "Verifications failed for task " + task + "." + "|@"));
            }
        }

        return result;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
