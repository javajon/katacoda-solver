package com.katacoda.solver;

import com.katacoda.solver.subcommands.*;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "solver", mixinStandardHelpOptions = true,
        versionProvider = com.katacoda.solver.models.VersionProvider.class,
        description = "An authoring tool and utility for the O'Reilly Challenges framework. Verify tasks, provide hints, and solve tasks in a Challenge. Works with the provided hints.md, verifications.sh, and solutions.sh as the supporting sources.\n",
        footer = "\nOnce solutions have been decrypted commands such as next, all, and until will solve the challenge. Before publication, the 'all' command must solve all tasks without error.",
        subcommands = {
                // Primary interactive commands
                SubcommandSolutions.class,
                SubcommandNext.class,
                SubcommandAll.class,
                SubcommandUntil.class,

                // Secondary interactive commands
                SubcommandVerify.class,
                SubcommandHint.class,
                SubcommandView.class,
                SubcommandReset.class,
                SubcommandStatus.class,

                // To help authors at writing time
                SubcommandCreate.class,
                SubcommandLint.class,

                // Called from challenge framework (hidden from CLI)
                SubcommandRequestTaskAdvance.class,
                SubcommandRequestHint.class
        })

public class SolverTopCommand {
}