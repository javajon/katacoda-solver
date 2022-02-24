package com.katacoda.solver.subcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.katacoda.solver.SolverTopCommand;
import com.katacoda.solver.models.*;
import com.katacoda.solver.models.mapping.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Additional validation ideas:
 * <p>
 * Image referenced not in assets
 * Image in assets not referenced
 * <p>
 * call katacoda cli or something for basic scenario verification
 * <p>
 * Not using the latest version of solver, see [releases](https://github.com/javajon/katacoda-solver/releases)
 * The number of steps to solutions, verifications and hints does not match.
 * <p>
 * Consider tool: npx mega-linter-runner : https://github.com/megalinter/megalinter
 */

@Command(name = "lint", commandListHeading = "Authoring", headerHeading = "XXXUsage:%n%n", description = "Determine required artifacts for challenge are present and correct in either authoring or challenge contexts.")
public class SubcommandLint implements Callable<Integer> {

    public static final String PUNCTUATION = "~!@#$%^&*-_=+<>?:\"{}|\\][;',./']";
    public static final List<String> DIFFICULTY_LEVELS = Arrays.asList("Beginner", "Intermediate", "Advanced");
    public static final List<String> IMAGE_IDS = Arrays.asList("ubuntu:1804",
            "ubuntu:2004",
            "kubernetes-cluster:1.18",
            "kubernetes-cluster-running:1.18",
            "openjdk:15",
            "kotlin:1.3.61",
            "python:3.6",
            "python:3.7",
            "python:3.8",
            "golang:1.14",
            "rust:1.44.1",
            "ruby:2.5",
            "dotnet:3.1",
            "rlang:3.6",
            "nodejs:12");

    @Spec
    CommandSpec spec;

    @CommandLine.Option(names = {"-l", "--level"}, required = false, description = "Optional name of directory for new challenge skaffold files. Default challenge-<archetype>.", defaultValue = "")
    private String level = "";

    @CommandLine.Option(names = {"-t", "--target"}, required = false, description = "Targeted path to directory for new challenge skaffold files. Missing directories will be created. Default current directory.", defaultValue = ".")
    private String target = ".";

    @CommandLine.Option(names = {"-sa", "--showAppendix"}, required = false, description = "Showing report appendices.", defaultValue = "false")
    private boolean showAppendix;

    @CommandLine.Option(names = {"-f", "--fix"}, required = false, description = "Apply fixes where appropriate and possible.", defaultValue = "false")
    private boolean fixRequested;


    /**
     * Directory where challenge is present for checking. Typically, working directory, but unit tests can override.
     */
    private Path targetPath = Path.of(target);

    @Override
    public Integer call() {

        targetPath = Path.of(target);
        if (!targetPath.toFile().exists()) {
            targetPath.toFile().mkdir();
        }

        Path projectName;
        try {
            projectName = targetPath.toRealPath().getFileName();
        } catch (IOException e) {
            projectName = targetPath;
        }
        Node<CheckItem> itemsTreeRoot = getInfoItem(String.format("Checking challenge '%s'.", projectName));
        itemsTreeRoot.add(getInfoItem("Checklist details are evolving. Feedback is welcome. See roadmap."));

        switch (Configuration.getContextType()) {
            case development:
                itemsTreeRoot.add(getErrorItem(String.format("Target path %s does not appear as a source directory for a challenge.", targetPath)));
                break;
            case authoring:
                authoringChecklist(itemsTreeRoot);
                break;
            case challenge:
                challengeChecklist(itemsTreeRoot);
                break;
        }

        Tallies tallies = new Tallies();
        StringBuilder report = new StringBuilder();
        report.append('\n');
        report.append("Challenge Authoring Inspection Checklist\n");
        report.append("----------------------------------------\n");

        dumpChecks(itemsTreeRoot, tallies, report);
        if (showAppendix) {
            report.append('\n');
            dumpAppendices(itemsTreeRoot, report);
        }
        report.append('\n');
        report.append(tallies.report());
        out().println(report);

        return tallies.hasErrors() ? 1 : 0;
    }

    private void dumpChecks(Node<CheckItem> item, Tallies tallies, StringBuilder report) {
        if (item != null) {
            report.append(item.getData().getStatus().message(item.getData().getMessage())).append('\n');
            tallies.add(item.getData().getStatus());
            Status.indent += 2;
            for (Node<CheckItem> subItem : item.getChildren()) {
                dumpChecks(subItem, tallies, report);
            }
            Status.indent -= 2;
        }
    }

    private void dumpAppendices(Node<CheckItem> item, StringBuilder report) {
        if (item != null) {
            if (!item.getData().getAppendix().isEmpty()) {
                report.append("=== Appendix Report ========================\n");
                report.append(item.getData().getAppendix()).append('\n');
            }
            for (Node<CheckItem> subItem : item.getChildren()) {
                dumpAppendices(subItem, report);
            }
        }
    }

    private Status findSeverestStatus(Node<CheckItem> item) {
        return findSeverestStatus(item, Status.Pass);
    }

    private Status findSeverestStatus(Node<CheckItem> item, Status severest) {
        if (item.getData().getStatus().ordinal() > severest.ordinal()) {
            severest = item.getData().getStatus();
        }

        for (Node<CheckItem> subItem : item.getChildren()) {
            if (findSeverestStatus(subItem, severest).ordinal() > severest.ordinal()) {
                severest = subItem.getData().getStatus();
            }
        }

        return severest;
    }


    private void authoringChecklist(Node<CheckItem> checks) {

        ScenarioIndex scenarioIndex = getScenarioIndex(checks);

        if (!Path.of("assets").toFile().exists()) {
            checks.add(getErrorItem("'assets' directory is missing."));
        }

        int steps = scenarioIndex.getDetails().getSteps().length;
        checks.add(checkHintScript());
        checks.add(checkVerifyScript());
        checks.add(checkHints(steps));
        checks.add(checkSolutions(steps));
        checks.add(checkVerifications(steps));

        checks.add(getInfoItem("Consider run with another linter using 'npx mega-linter-runner'"));

        applyGroupStatus(checks);
    }

    private ScenarioIndex getScenarioIndex(Node<CheckItem> checks) {
        Node<CheckItem> indexJsonChecks = checks.add(getItem("Checking index.json."));

        indexJsonChecks.add(isIndexPresent() ? getPassItem("File found.") : getErrorItem("File not found."));

        ScenarioIndex scenarioIndex;
        try {
            scenarioIndex = loadIndexJson();
            indexJsonChecks.add(getPassItem("Is valid json."));
        } catch (IOException e) {
            indexJsonChecks.add(getErrorItem(String.format("Could not be read: %s.", e.getMessage())));
            scenarioIndex = new ScenarioIndex();
        }

        indexJsonChecks.add(checkType(scenarioIndex.getType()));
        indexJsonChecks.add(checkTitle(scenarioIndex.getTitle()));
        indexJsonChecks.add(checkDescription(scenarioIndex.getDescription()));
        indexJsonChecks.add(checkTime(scenarioIndex.getTime()));
        indexJsonChecks.add(checkDifficulty(scenarioIndex.getDifficulty()));
        indexJsonChecks.add(checkDetails(scenarioIndex.getDetails()));
        indexJsonChecks.add(checkBackend(scenarioIndex.getBackend()));
        return scenarioIndex;
    }

    private Node<CheckItem> checkHintScript() {
        File script = Paths.get(target, "hint.sh").toFile();
        Node<CheckItem> checks = getItem(String.format("Checking %s", script));

        String content = readFile(script, checks);
        if (!content.isEmpty()) {
            checkScript(script, checks);
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem>checkVerifyScript() {
        File script = Paths.get(target, "verify.sh").toFile();
        Node<CheckItem> checks = getItem(String.format("Checking %s", script));

        String content = readFile(script, checks);
        if (!content.isEmpty()) {
            checkScript(script, checks);
        }

        return applyGroupStatus(checks);
    }


    private String validateScript(Path script) {
        return run("shellcheck", script.toAbsolutePath().toString());
    }

    private Node<CheckItem> checkHints(int steps) {
        File markdown = Paths.get(target, "assets", "hints.md").toFile();
        Node<CheckItem> checks = getItem(String.format("Checking %s", markdown));

        String content = readFile(markdown, checks);
        if (!content.isEmpty()) {
            for (int step = 1; step <= steps; step++) {
                String function = String.format("## Task %d, Hint ", step);
                if (!content.contains(function)) {
                    checks.add(getErrorItem(String.format("Expected hint ('## Task %d, Hint ') not found: %s.", step, function)));
                }
            }
        }

        return applyGroupStatus(checks);
    }

    private String readFile(File source, Node<CheckItem> checks) {
        if (source.exists()) {
            if (source.length() != 0) {
                try {
                    return Files.readString(source.toPath(), StandardCharsets.US_ASCII);
                } catch (IOException e) {
                    checks.add(getErrorItem(String.format("Could not read %s.", e.getMessage())));
                }
            } else {
                checks.add(getErrorItem(String.format("File %s is empty.", source)));
            }
        } else {
            checks.add(getErrorItem(String.format("File %s is missing.", source)));
        }

        return "";
    }

    private Node<CheckItem> checkSolutions(int steps) {
        File script = Paths.get(target, "assets", "solutions.sh").toFile();
        Node<CheckItem> checks = getItem(String.format("Checking %s", script));

        String content = readFile(script, checks);
        if (!content.isEmpty()) {
            for (int step = 1; step <= steps; step++) {
                String function = String.format("function solve_task_%d()", step);
                if (!content.contains(function)) {
                    checks.add(getErrorItem(String.format("Expected function not found: %s.", function)));
                }
            }

            if (isEncryptionNeeded()) {
                checks.add(getWarningItem("Solutions.sh needs to be encrypted."));
                if (fixRequested) {
                    SolverTopCommand cli = new SolverTopCommand();
                    CommandLine cmd = new CommandLine(cli);
                    int exitCode = cmd.execute("solutions", "--encrypt");
                    if (exitCode == 0) {
                        checks.add(getFixedItem("Encryption applied to solutions.sh as request by `--fix`."));
                    } else {
                        checks.add(getErrorItem("Failed to encrypt solutions.sh as request by `--requestEncrypt`."));
                    }
                }
            }

            checkScript(script, checks);
        }

        return applyGroupStatus(checks);
    }

    private String run(String... command) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        builder.directory(new File(System.getProperty("user.home")));
        Process process;
        try {
            process = builder.start();
            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            return output.toString();
        } catch (IOException | InterruptedException e) {
            return e.getMessage();
        }
    }

    private void checkScript(File script, Node<CheckItem> checks) {
        String scriptSuggestions = validateScript(script.toPath());
        if (!scriptSuggestions.isEmpty()) {
            Node<CheckItem> scriptWarning = getWarningItem(showAppendix ? "Script checks appear at bottom of report." : "Add --showAppendix or -sa to see script lint report");
            scriptWarning.getData().setAppendix(scriptSuggestions);
            checks.add(scriptWarning);
        }
    }

    private Node<CheckItem> checkVerifications(int steps) {
        File script = Paths.get(target, "assets", "verifications.sh").toFile();
        Node<CheckItem> checks = getItem(String.format("Checking %s.", script));

        String content = readFile(script, checks);
        if (!content.isEmpty()) {

            for (int step = 1; step <= steps; step++) {
                String function = String.format("function verify_task_%d()", step);
                if (!content.contains(function)) {
                    checks.add(getErrorItem(String.format("Expected function not found: %s", function)));
                }
            }
        }

        return applyGroupStatus(checks);
    }


    private boolean isEncryptionNeeded() {
        File solutionsSh = Paths.get(target, "assets", "solutions.sh").toFile();
        File solutionsShEnc = Paths.get(target, "assets", "solutions.sh.enc").toFile();

        return !solutionsShEnc.exists() || (solutionsShEnc.lastModified() < solutionsSh.lastModified());
    }

    private Node<CheckItem> checkDetails(Details details) {
        Node<CheckItem> checks = getItem("Checking assets.");

        checks.add(checkIntro(details.getIntro()));
        checks.add(checkFinish(details.getFinish()));
        checks.add(checkSteps(details.getSteps()));
        checks.add(checkAssets(details.getAssets().getHost01()));

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkSteps(Page[] steps) {
        Node<CheckItem> checks = getItem("Checking Steps.");

        if (steps.length == 0) {
            checks.add(getErrorItem("No steps are defined."));
        } else if (steps.length <= 2 || steps.length > 10) {
            checks.add(getWarningItem("Consider defining 3-10 steps in this challenge."));
        }

        int count = 0;
        for (Page step : steps) {
            count++;
            if (step.getTitle().trim().isEmpty()) {
                checks.add(getErrorItem(String.format("Title is missing for step %d.", count)));
            }
            if (step.getTitle().trim().length() > 40) {
                checks.add(getWarningItem(String.format("Title exceeds 40 characters %d.", count)));
            }
            if (step.getTitle().toLowerCase().trim().startsWith("step ")) {
                checks.add(getErrorItem("Starting a step title with `step ` is clutter, remove."));
            }

            if (step.getText().trim().isEmpty()) {
                checks.add(getErrorItem(String.format("Step `text:` markdown file reference is missing for step %d.", count)));
            }
            if (!Paths.get(step.getText()).toFile().exists()) {
                checks.add(getErrorItem(String.format("Step `text:` markdown file %s is missing for step %d.", step.getText(), count)));
            }

            if (step.getVerify().trim().isEmpty()) {
                checks.add(getErrorItem(String.format("Step `verify:` file reference is missing for step %d.", count)));
            }
            if (!Paths.get(step.getVerify()).toFile().exists()) {
                checks.add(getErrorItem(String.format("Step `verify:` file %s is missing for step %d.", step.getVerify(), count)));
            }

            if (step.getHint().trim().isEmpty()) {
                checks.add(getErrorItem(String.format("Step `hint:` file reference is missing for step %d.", count)));
            }
            if (!Paths.get(step.getHint()).toFile().exists()) {
                checks.add(getErrorItem(String.format("Step `hint:` file %s is missing for step %d.", step.getHint(), count)));
            }
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkIntro(Page intro) {
        Node<CheckItem> checks = getItem("Checking intro page.");

        // todo - common method to check a string setting
        if (!intro.getTitle().trim().isEmpty()) {
            checks.add(getErrorItem("Title not needed for page."));
        }
        if (intro.getText().trim().isEmpty()) {
            checks.add(getErrorItem("`text:` markdown file reference is missing."));
        }

        // todo call common method to check file
        if (!Paths.get(intro.getText()).toFile().exists()) {
            checks.add(getErrorItem("`text:` markdown file %s is missing."));
        }
        if (Paths.get(intro.getText()).toFile().length() == 0) {
            checks.add(getErrorItem("`text:` markdown file is empty."));
        }

        // TODO
//        intro.getCourseData()
//
//                // todo check setting and file
//        // todo - make sure script downloads solver and correct version
//        "courseData": "init-background.sh",
//                "code": "init-foreground.sh",

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkFinish(Page finish) {
        Node<CheckItem> checks = getItem("Checking finish page.");

        if (!finish.getTitle().trim().isEmpty()) {
            checks.add(getErrorItem("Title not needed for page."));
        }
        if (finish.getText().trim().isEmpty()) {
            checks.add(getErrorItem("`text:` markdown file reference is missing."));
        }
        if (!Paths.get(finish.getText()).toFile().exists()) {
            checks.add(getErrorItem("`text:` markdown file %s is missing."));
        }
        if (Paths.get(finish.getText()).toFile().length() == 0) {
            checks.add(getErrorItem("`text:` markdown file is empty."));
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkAssets(AssetFile[] assets) {
        Node<CheckItem> checks = getItem("Checking host01 required assets.");

        if (assets.length == 0) {
            checks.add(getErrorItem("List of files under assets.host01 are missing."));
        } else {
            boolean foundVerifications = false;
            boolean foundHints = false;
            boolean foundSolutions = false;

            for (AssetFile assetFile : assets) {
                if (assetFile.getFile().equals("verifications.sh")) {
                    foundVerifications = true;
                    if (!assetFile.getTarget().startsWith("/usr/local/bin")) {
                        checks.add(getErrorItem("verifications.sh must be copied to \"/usr/local/bin/\"."));
                    }
                    if (!assetFile.getChmod().equals("+x")) {
                        checks.add(getErrorItem("verifications.sh must have chmod of `+x`."));
                    }
                }

                if (assetFile.getFile().equals("hints.md")) {
                    foundHints = true;
                    if (!assetFile.getTarget().startsWith("/opt")) {
                        checks.add(getErrorItem("hints.md must be copied to /opt."));
                    }
                    if (!assetFile.getChmod().isEmpty()) {
                        checks.add(getWarningItem("hints.md must have not have a chmod defined."));
                    }
                }

                if (assetFile.getFile().equals("solutions.sh.enc")) {
                    foundSolutions = true;
                    if (!assetFile.getTarget().startsWith("/opt")) {
                        checks.add(getErrorItem("solutions.md.enc must be copied to /opt."));
                    }
                    if (!assetFile.getChmod().isEmpty()) {
                        checks.add(getWarningItem("solutions.sh.enc must have not have a chmod defined."));
                    }
                }
            }

            if (!foundVerifications) {
                checks.add(getErrorItem("verifications.sh missing as a copied asset."));
            }
            if (!foundHints) {
                checks.add(getErrorItem("hints.md missing as a copied asset."));
            }
            if (!foundSolutions) {
                checks.add(getErrorItem("solutions.md.enc missing as a copied asset."));
            }
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkBackend(Backend backend) {
        Node<CheckItem> checks = getItem("Checking Backend Group.");

        String imageid = backend.getImageid();
        if (imageid.trim().isEmpty()) {
            checks.add(getErrorItem("backend.imageid is missing from index.json."));
        } else if (!IMAGE_IDS.contains(imageid)) {
            checks.add(getWarningItem(String.format("backend.imageid %s not one of the documented supported environments.", imageid)));
            checks.add(getInfoItem("See documentation for list of valid imageids: https://www.katacoda.community/essentials/environments.html#supported-environments."));
        }

        if (imageid.trim().length() != imageid.length()) {
            checks.add(getErrorItem("Remove leading or trailing whitespace from imageid."));
        }

        return applyGroupStatus(checks);
    }


    private Node<CheckItem> checkTime(String time) {
        Node<CheckItem> checks = getItem(String.format("Checking estimated time: `%s`.", time));

        if (time.trim().isEmpty()) {
            checks.add(getErrorItem("Time is missing from index.json."));
        }

        if (!time.endsWith(" minutes")) {
            checks.add(getErrorItem("Time must end with ` minutes`. Such as `15 minutes`."));
        } else {
            String minutesToken = time.substring(0, time.indexOf(' '));
            int minutes = Integer.parseInt(minutesToken);
            if ((minutes % 5) != 0) {
                checks.add(getErrorItem("Time must be set at a 5 minute increment. e.g. 5, 10, 15."));
            }

            if (minutes < 5) {
                checks.add(getErrorItem("Time must not be less than 5 minutes. Perhaps the challenge is too easy."));
            }

            if (minutes > 45) {
                checks.add(getErrorItem("Time must not be greater than 45 minutes. Perhaps the challenge is too difficult."));
            }
        }

        if (time.trim().length() != time.length()) {
            checks.add(getErrorItem("Remove leading or trailing whitespace from type."));
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkDifficulty(String difficulty) {
        Node<CheckItem> checks = getItem(String.format("Checking difficulty: `%s`.", difficulty));

        if (difficulty.trim().isEmpty()) {
            checks.add(getErrorItem("Type is missing from index.json."));
        }

        if (!DIFFICULTY_LEVELS.contains(difficulty)) {
            checks.add(getErrorItem("Difficulty level must be one of these choices: " + DIFFICULTY_LEVELS));
        }

        if (difficulty.trim().length() != difficulty.length()) {
            checks.add(getErrorItem("Remove leading or trailing whitespace from type."));
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkType(String type) {
        Node<CheckItem> checks = getItem(String.format("Checking type: `%s`.", type));
        checks.add(getInfoItem("See documentation for latest version: https://www.katacoda.community/challenges/challenges.html#_1-specify-the-challenge-version"));

        if (type.trim().isEmpty()) {
            checks.add(getErrorItem("Type is missing from index.json."));
        }

        if (!type.startsWith("challenge@")) {
            checks.add(getErrorItem("Type must start with `challenge@`."));
        }

        if (type.trim().length() != type.length()) {
            checks.add(getErrorItem("Remove leading or trailing whitespace from type."));
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkDescription(String description) {
        Node<CheckItem> checks = getItem(String.format("Checking description: `%s`", description));

        if (description.trim().isEmpty()) {
            checks.add(getErrorItem("Description is missing from index.json."));
        }

        if (description.length() < 10) {
            checks.add(getErrorItem("Description is too short at less than 10 characters."));
        }

        if (description.length() > 120) {
            checks.add(getErrorItem("Description is too long at more than 120 characters."));
        }

        if (description.trim().length() != description.length()) {
            checks.add(getErrorItem("Remove leading or trailing whitespace from description."));
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> checkTitle(String title) {
        Node<CheckItem> checks = getItem(String.format("Checking title: `%s`.", title));

        if (title.trim().isEmpty()) {
            checks.add(getErrorItem("Title is missing from index.json."));
        } else if (!title.contains(": ")) {
            checks.add(getErrorItem("Title is missing the subject category. Such as `subject: title`."));
        }

        if (title.length() < 10) {
            checks.add(getErrorItem("Title is too short at less than 10 characters."));
        }

        if (title.length() > 60) {
            checks.add(getErrorItem("Title is too long at more than 60 characters."));
        }

        if (title.trim().length() != title.length()) {
            checks.add(getErrorItem("Remove leading or trailing whitespace from title."));
        }

        return applyGroupStatus(checks);
    }

    private Node<CheckItem> applyGroupStatus(Node<CheckItem> checks) {
        Status status = findSeverestStatus(checks);
        String message = checks.getData().getMessage();

        checks.setData(new CheckItem(status, message));

        return checks;
    }

    private ScenarioIndex loadIndexJson() throws IOException {
        // create object mapper instance
        ObjectMapper mapper = new ObjectMapper();

        // Map JSON into the object
        return mapper.readValue(Paths.get(targetPath.toString(), "index.json").toFile(), ScenarioIndex.class);
    }

    private boolean isIndexPresent() {
        return new File("index.json").exists();
    }

    private void challengeChecklist(Node<CheckItem> checks) {
    }

    private PrintWriter out() {
        return spec.commandLine().getOut();
    }

    private Node<CheckItem> getErrorItem(String message) {
        return new Node<>(new CheckItemBuilder().message(message).status(Status.Error).create());
    }

    private Node<CheckItem> getFixedItem(String message) {
        return new Node<>(new CheckItemBuilder().message(message).status(Status.Fixed).create());
    }

    private Node<CheckItem> getWarningItem(String message) {
        return new Node<>(new CheckItemBuilder().message(message).status(Status.Warning).create());
    }

    private Node<CheckItem> getInfoItem(String message) {
        return new Node<>(new CheckItemBuilder().message(message).status(Status.Info).create());
    }

    private Node<CheckItem> getItem(String message) {
        return new Node<>(new CheckItemBuilder().message(message).create());
    }

    private Node<CheckItem> getPassItem(String message) {
        return new Node<>(new CheckItemBuilder().message(message).status(Status.Pass).create());
    }
}
