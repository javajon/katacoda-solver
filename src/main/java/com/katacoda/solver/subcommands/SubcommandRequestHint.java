package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.Hints;
import com.katacoda.solver.models.Verifications;
import org.jboss.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "request_hint", hidden = true, description = "Request hint for task an verification number. Called by challenge framework, not by people.")
public class SubcommandRequestHint implements Callable<Integer> {
    private static final Logger LOG = Logger.getLogger(SubcommandRequestHint.class);

    @Spec
    CommandSpec spec;

    @Parameters(index = "0", defaultValue = "0", description = "Seconds since task prompted. (currently ignored)")
    private int stepUptime = 0;

    @Parameters(index = "1", defaultValue = "0", description = "Display hint for task.")
    private int task = 0;

    @Parameters(index = "2", defaultValue = "0", description = "Hint number of task to show")
    private int hint = 0;

    @Override
    public Integer call() {

        // TODO consider stepUptime with delay

        Hints hints = new Hints();

        if (task == 0) {
            task = Configuration.getCurrentTask();
        }

        if (hint == 0) {
            new Verifications().verify(task);
        }

        LOG.info(String.format("Getting hint for task %d, verification %d.", task, hint));

        out().printf(hints.getHint(task, hint));

        return 0;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
