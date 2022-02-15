package com.katacoda.solver;

import com.katacoda.solver.models.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolverCLITests {

    private final String REG_EX_SEM_VER = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";

    private SolverTopCommand cli;

    private StringWriter sw;

    private StringWriter swErr;

    private CommandLine cmd;

    private void clearOutput() {
        cmd.getOut().flush();
        cmd.getErr().flush();
        sw.getBuffer().setLength(0);
        swErr.getBuffer().setLength(0);
    }

    @BeforeEach
    void setUp() {
        cli = new SolverTopCommand();
        cmd = new CommandLine(cli);

        sw = new StringWriter();
        swErr = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        cmd.setErr(new PrintWriter(swErr));
    }

    @Test
    public void version()  {
        int exitCode = cmd.execute("--version");
        assertEquals(0, exitCode);
        if (!sw.toString().contains("-")) {
            String[] splitted = sw.toString().trim().split(" ");
            assertTrue(splitted[splitted.length - 1].matches(REG_EX_SEM_VER), "Expecting version " + REG_EX_SEM_VER + " but found " + sw.toString().trim());
        }
    }

    @Test
    public void help() {
        int exitCode = cmd.execute("--help");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().trim().contains("Usage: solver"));
        assertTrue(sw.toString().trim().contains("-h, --help"));
        assertTrue(sw.toString().trim().contains("-V, --version"));
        assertTrue(sw.toString().trim().contains("Commands:"));
    }

    @Test
    public void statusFull()  {
        int exitCode = cmd.execute("status");
        assertEquals(0, exitCode);
        assertEquals("Challenge is incomplete and next task to solve is 1.", sw.toString().trim());
    }

    @Test
    public void statusQuiet() {
        int exitCode = cmd.execute("status", "--quiet");
        assertEquals(0, exitCode);
        assertEquals("1", sw.toString().trim());
    }

    @Test
    public void reset() {
        Configuration.setCurrentTask(2);
        int exitCode = cmd.execute("reset");
        assertEquals(0, exitCode);
        assertEquals("The current task has been set back to the first step (1).", sw.toString().trim());
    }

    @Disabled
    @Test
    public void next() {
        int exitCode = cmd.execute("next");
        assertEquals(0, exitCode);
        assertEquals("Task 1 verification\n" +
                "Verifications passed for task 1.", sw.toString().trim());
    }

    @Disabled
    @Test
    public void subcommandRequestTaskAdvance() throws Exception {
        int exitCode = cmd.execute("challenge_advance_task", "1");
        assertEquals(0, exitCode);
        assertEquals("Verifications passed for task 1.", sw.toString().trim());

        Configuration.resetCurrentTask();
    }

    @Test
    public void hint() {
        int exitCode = cmd.execute("hint", "1", "1");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().trim().startsWith("A Deployment called `redis` has not been rolled out yet to the default namespace."));

        clearOutput();

        exitCode = cmd.execute("hint", "99", "99");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().startsWith("No hint found for task 99, hint 99."));
    }

    @Test
    public void quietHint(){
        int exitCode = cmd.execute("hint", "1", "1", "--quiet");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().trim().startsWith("A Deployment called `redis` has not been rolled out yet to the default namespace."));

        clearOutput();

        exitCode = cmd.execute("hint", "99", "99", "--quiet");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().isEmpty());
    }

    @Disabled
    @Test
    public void verify() throws Exception {
        File tester = new File("test.txt");

        int exitCode = cmd.execute("verify", "1");
        assertEquals(1, exitCode);

        touch(tester);

        exitCode = cmd.execute("verify", "1");
        assertEquals(0, exitCode);

        tester.delete();
    }

    @Test
    public void view()  {
        int exitCode = cmd.execute("view", "1");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().contains("Verifications for"));
        assertTrue(sw.toString().contains("Hints for"));
        assertTrue(sw.toString().contains("Solutions for"));
    }

    @Test
    public void viewInvalidTask()  {
        int exitCode = cmd.execute("view", "99");
        assertEquals(-1, exitCode);
        assertTrue(sw.toString().contains("No information is available for requested task 99"));
    }

    @Test
    public void solutionsEncryptDecrypt() {
        int exitCode = cmd.execute("sol", "-e");
        assertEquals(-0, exitCode);

        String message = sw.toString();
        int start = message.indexOf("passcode: `") + 11;
        String key = message.substring(start, start + 16);

        exitCode = cmd.execute("solutions", "-d", key);
        assertEquals(-0, exitCode);

        File tmp = new File(System.getProperty("java.io.tmpdir"));
        new File(tmp, "solutions.sh.enc").delete();
        new File(tmp, "solutions.sh").delete();
    }

    @Test
    public void create() throws IOException {

        // Scrub testing directory in the build location
        String testingDirectory =  "build/test-challenges";
        if (new File(testingDirectory).exists()) {
            Files.walk(Path.of(".", testingDirectory))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        // Create new
        int exitCode = cmd.execute("create", "--archetype", "linux", "--target", testingDirectory);
        assertEquals(0, exitCode);

        // Type to create without force
        exitCode = cmd.execute("create", "--archetype=linux", "--target", testingDirectory);
        assertEquals(2, exitCode);

        exitCode = cmd.execute("create", "--archetype=linux", "--force", "--target", testingDirectory);
        assertEquals(0, exitCode);

        Path project = Path.of(testingDirectory.toString(), "challenge-linux");
        assertTrue(project.toFile().exists());

        exitCode = cmd.execute("create", "--archetype=basic", "--target", testingDirectory);
        assertEquals(3, exitCode);

        exitCode = cmd.execute("create", "--archetype=kubernetes", "--target", testingDirectory);
        assertEquals(3, exitCode);
    }

    private static void touch(File file) throws IOException {
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }
    }
}
