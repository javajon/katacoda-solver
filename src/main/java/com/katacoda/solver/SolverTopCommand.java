package com.katacoda.solver;

import com.katacoda.solver.subcommands.*;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.logging.Level;
import java.util.logging.Logger;

@TopCommand
@Command(name = "solver", mixinStandardHelpOptions = true,
        versionProvider = com.katacoda.solver.models.VersionProvider.class,
        description = "Solves each task in the challenge given the provided functions for the task solutions and verifications.\n",
        footer = "\nNormally, tasks are solved sequentially using 'next'. However, some tasks can be skipped if a task is optional. Before publication, the 'all' command should solve all tasks without error.",
        subcommands = {
                // Primary interactive commands
                SubcommandSolutions.class,
                SubcommandNext.class,
                SubcommandAll.class,
                SubcommandUntil.class,

                // Secondary interactive commands
                SubcommandSolve.class,
                SubcommandVerify.class,
                SubcommandHint.class,
                SubcommandView.class,
                SubcommandReset.class,
                SubcommandYat.class,

                // To help authors at writing time
                SubcommandCreate.class,
                SubcommandChecklist.class,

                // Called from challenge framework (hidden from CLI)
                SubcommandRequestTaskAdvance.class
        })


public class SolverTopCommand {

    // option is shared with subcommands
    @CommandLine.Option(names = {"-l", "--verbose"}, scope = CommandLine.ScopeType.INHERIT)
    public void setVerbose(boolean[] verbose) {
        // Configure log4j.
        // (This is a simplistic example: a real application may use more levels and
        // perhaps configure only the ConsoleAppender level instead of the root log level.)
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(verbose.length > 0 ? Level.FINEST : Level.INFO);
    }
}
