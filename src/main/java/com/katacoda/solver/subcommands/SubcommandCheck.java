package com.katacoda.solver.subcommands;

import com.katacoda.solver.models.Configuration;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

@Command(name = "check", commandListHeading = "Authoring", headerHeading = "XXXUsage:%n%n", description = "Determine required artifacts for challenge are present and correct in either authoring or challenge contexts.")
public class SubcommandCheck implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    enum Status {
        Pass('✅', "green"),
        Warning('⚠', "yellow"),
        Error('❌', "red"),
        Undetermined('☐', "red");

        private final char icon;
        private final String color;

        Status(char icon, String color) {
            this.icon = icon;
            this.color = color;
        }

        public String getStatus(String message) {
            return CommandLine.Help.Ansi.AUTO.string(String.format("@|bold,%s %s  %-8s %s|@%n", color, icon, name(), message));
        }
    }

    @Override
    public Integer call() {

        out().println("Challenge Authoring Inspection Checklist");
        out().println("----------------------------------------\n");

        out().println(Status.Warning.getStatus("Checklist details are evolving. See roadmap."));

        switch (Configuration.getContextType()) {
            case development:
                out().println(Status.Warning.getStatus("Checklist is not for the solver development environment."));
                break;
            case authoring:
                authoringChecklist();
                break;
            case challenge:
                challengeChecklist();
                break;
        }

        return 0;
    }

    private String show(Status status, String message) {
        return status.getStatus(message);
    }

    private void authoringChecklist() {
        out().printf(isIndexPresent().getStatus("index.json is present and valid json."));
        out().printf(isIndexValid().getStatus("index.json is valid json."));
        out().println(Status.Undetermined.getStatus("index.json contains \"type\": \"challenge@0.8\""));
        out().println(Status.Undetermined.getStatus("index.json copies the asset verifications.sh file to /usr/local/bin."));
        out().println(Status.Undetermined.getStatus("index.json copies the asset hints.md file to /opt."));
        out().println(Status.Undetermined.getStatus("index.json copies the asset solutions.sh.ptc file to /opt."));
        out().println(Status.Undetermined.getStatus("index.json contains type "));

        out().println(isHintScriptPresent().getStatus("hint.sh is present."));
        out().println(isHintScriptValid().getStatus("hint.sh is a valid bash shell script."));
        out().println(isVerifyScriptPresent().getStatus("verify.sh is present."));
        out().println(isVerifyScriptValid().getStatus("verify.sh is a valid bash shell script."));
        out().println(isVerificationsScriptPresent().getStatus("verifications.sh is present."));
        out().println(isVerificationsScriptValid().getStatus("verifications.sh is valid bash shell script."));
        out().println(isIntroMarkdown().getStatus("intro.md is present and valid markdown."));
        out().println(isFinishMarkdown().getStatus("finish.md is present and valid markdown."));
        out().println(isHintsMarkdown().getStatus("hints.md is present and valid markdown."));
        out().println(isSolutionsEncrypted().getStatus("solutions.enc encrypted solutions file is present."));
        out().println(isSolutionsPasscode().getStatus("solutions.enc passcode is not recorded in solutions-passcode.md."));

        out().println(Status.Undetermined.getStatus("In index.json steps x is missing {title, text, verify, hint} setting."));
        out().println(Status.Undetermined.getStatus("In index.json steps x is references missing artifact for { text, verify, hint }."));

        out().println(Status.Undetermined.getStatus(".cypress scripts are present and valid."));
        out().println(Status.Undetermined.getStatus("scenario image reference is valid."));

        out().println(Status.Undetermined.getStatus("Not using latest version of solver, see [releases](https://github.com/javajon/katacoda-solver/releases)."));

        out().println(Status.Undetermined.getStatus("The number of number of steps to solutions, verifications and hints does not match."));
    }

    private Status isSolutionsPasscode() {
        return new File("assets/solutions-passcode.md").exists() ? Status.Pass : Status.Error;
    }

    private Status isSolutionsEncrypted() {
        return new File("assets/solutions.enc").exists() ? Status.Pass : Status.Error;
    }

    private Status isHintsMarkdown() {
        return new File("assets/hints.md").exists() ? Status.Pass : Status.Error;
    }

    private Status isIntroMarkdown() {
        return new File("intro.md").exists() ? Status.Pass : Status.Error;
    }

    private Status isFinishMarkdown() {
        return new File("finish.md").exists() ? Status.Pass : Status.Error;
    }

    private Status isVerificationsScriptValid() {
        // TODO - shellcheck
        return Status.Undetermined;
    }

    private Status isVerificationsScriptPresent() {
        return new File("assets/verifications.sh").exists() ? Status.Pass : Status.Error;
    }

    private Status isVerifyScriptValid() {
        // TODO - shellcheck
        return Status.Undetermined;
    }

    private Status isVerifyScriptPresent() {
        return new File("verify.sh").exists() ? Status.Pass : Status.Error;
    }

    private Status isHintScriptValid() {
        // TODO - shellcheck
        return Status.Undetermined;
    }

    private Status isIndexValid() {
        return Status.Undetermined;
    }

    private Status isHintScriptPresent() {
        return new File("hints.sh").exists() ? Status.Pass : Status.Error;
    }

    private Status isIndexPresent() {
        // TODO - validate json
        return new File("index.json").exists() ? Status.Pass : Status.Error;
    }

    private void challengeChecklist() {
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }
}
