package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import com.katacoda.solver.models.Hints;
import com.katacoda.solver.models.Verifications;
import org.jboss.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "hint", description = "Get hint give a task number and hint number.")
public class SubcommandHint implements Callable<Integer> {
    private static final Logger LOG = Logger.getLogger(SubcommandHint.class);

    @Spec CommandSpec spec;

    @Parameters(index = "0", defaultValue = "0", description = "Display hint for task.")
    private int task = 0;

    @Parameters(index = "1", defaultValue = "0", description = "Hint number of task to show, default is 1 for nn-current task or current hint for current task.")
    private int hint = 0;

    @Option(names = {"-m", "--mute"}, description = "Stop revealing the hints.")
    private boolean mute = false;

    @Option(names = {"-q", "--quiet"}, required = false, description = "Output formatted for challenge interface.")
    private boolean quiet = false;

    @Override
    public Integer call() {

        Hints hints = new Hints();
        hints.enable(!mute);

        if (task == 0) {
            task = Configuration.getCurrentTask();
        }

        if (hint == 0) {
            new Verifications().verify(task);
        }

        LOG.info(String.format("Getting hint for task %d, verification %d.", task, hint));

        if (quiet) {
            out().printf(hints.getHint(task, hint));
        } else {
            String hintResult = hints.getHint(task, hint);
            if (hintResult.isEmpty()) {
                out().printf("No hint found for task %d, hint %d.\n", task, hint);
            }
            else {
                out().println(Ansi.AUTO.string("@|bold,yellow " + hintResult + "|@"));
            }
        }

        return 0;
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
