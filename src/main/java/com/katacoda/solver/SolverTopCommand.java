package com.katacoda.solver;

import com.katacoda.solver.subcommands.*;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

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
                SubcommandStatus.class,

                // To help authors at writing time
                SubcommandCreate.class,
                SubcommandCheck.class,

                // Called from challenge framework (hidden from CLI)
                SubcommandRequestTaskAdvance.class,
                SubcommandRequestHint.class
        })


public class SolverTopCommand {
}
