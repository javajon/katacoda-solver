package com.katacoda.solver;

import com.katacoda.solver.models.Configuration;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolverCLITests {

    private static final Logger LOG = Logger.getLogger(SolverCLITests.class);

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
    void setUp() throws Exception {
        cli = new SolverTopCommand();
        cmd = new CommandLine(cli);

        sw = new StringWriter();
        swErr = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        cmd.setErr(new PrintWriter(swErr));
    }

    @Test
    public void version() throws Exception {
        int exitCode = cmd.execute("--version");
        assertEquals(0, exitCode);
        if (!sw.toString().contains("-")) {
            String[] splitted = sw.toString().trim().split(" ");
            assertTrue(splitted[splitted.length - 1].matches(REG_EX_SEM_VER), "Expecting version " + REG_EX_SEM_VER + " but found " + sw.toString().trim());
        }
    }

    @Test
    public void help() throws Exception {
        int exitCode = cmd.execute("--help");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().trim().contains("Usage: solver"));
        assertTrue(sw.toString().trim().contains("-h, --help"));
        assertTrue(sw.toString().trim().contains("-V, --version"));
        assertTrue(sw.toString().trim().contains("Commands:"));
    }

    @Test
    public void statusFull() throws Exception {
        int exitCode = cmd.execute("status");
        assertEquals(0, exitCode);
        assertEquals("Challenge is incomplete and next task to solve is 1.", sw.toString().trim());
    }

    @Test
    public void statusQuiet() throws Exception {
        int exitCode = cmd.execute("status", "--quiet");
        assertEquals(0, exitCode);
        assertEquals("1", sw.toString().trim());
    }

    @Test
    public void reset() throws Exception {
        Configuration.setCurrentTask(2);
        int exitCode = cmd.execute("reset");
        assertEquals(0, exitCode);
        assertEquals("The current task has been set back to the first step (1).", sw.toString().trim());
    }

    @Disabled
    @Test
    public void next() throws Exception {
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
    public void hint() throws Exception {
        int exitCode = cmd.execute("hint", "1", "1");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().trim().startsWith("A Deployment called `redis` has not been rolled out yet to the default namespace."));

        clearOutput();

        exitCode = cmd.execute("hint", "99", "99");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().startsWith("No hint found for task 99, hint 99."));
    }

    @Test
    public void quietHint() throws Exception {
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
    public void view() throws Exception {
        int exitCode = cmd.execute("view", "1");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().contains("Verifications for"));
        assertTrue(sw.toString().contains("Hints for"));
        assertTrue(sw.toString().contains("Solutions for"));
    }

    @Test
    public void viewInvalidTask() throws Exception {
        int exitCode = cmd.execute("view", "99");
        assertEquals(-1, exitCode);
        assertTrue(sw.toString().contains("No information is available for requested task 99"));
    }

    @Test
    public void solutionsEncryptDecrypt() throws Exception {
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
    public void create() throws Exception {

        Path tmpTest = Path.of(System.getProperty("java.io.tmpdir"), "test-create");

        // Clean output target
        if (tmpTest.toFile().exists()) {
            boolean deleteSuccess = removeAll(tmpTest.toFile());
            assertTrue(deleteSuccess);
        }

        // Create new
        int exitCode = cmd.execute("create", "--archetype=linux", "--destination=" + tmpTest.toFile());
        assertEquals(0, exitCode);

        // Type to create without force
        exitCode = cmd.execute("create", "--archetype=linux", "--destination=" + tmpTest.toFile());
        assertEquals(2, exitCode);

        exitCode = cmd.execute("create", "--archetype=linux", "--destination=" + tmpTest.toFile(), "--force");
        assertEquals(0, exitCode);

        Path project = Path.of(tmpTest.toString(), "challenge-linux-solver");
        assertTrue(project.toFile().exists());

        tmpTest.toFile().deleteOnExit();
    }

    private boolean removeAll(File file) {
        // Safety first
        if (!file.getAbsolutePath().startsWith(System.getProperty("java.io.tmpdir"))) {
            return false;
        }

        File[] contents = file.listFiles();
        if (contents != null) {
            for (File subFile : contents) {
                if (!Files.isSymbolicLink(subFile.toPath())) {
                    if (!removeAll(subFile)) {
                        return false;
                    }
                }
            }
        }

        return file.delete();
    }

    private static void touch(File file) throws IOException {
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }
    }
}
