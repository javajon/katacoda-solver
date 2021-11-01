package com.katacoda.solver.models;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class VerificationsTest {

    private static final String BASH_BANG = "#!/bin/bash";

    @Test
    void requestTaskAdvance() throws IOException {
        assertEquals(1, new Verifications().requestTaskAdvance(Configuration.getCurrentTask()));
        File file = new File("test.txt");
        touch(file);
        file.deleteOnExit();
        assertEquals(0, new Verifications().requestTaskAdvance(Configuration.getCurrentTask()));
        assertEquals(2, Configuration.getCurrentTask());
        Configuration.resetCurrentTask();
    }

    @Test
    void getSource() {
        String source = new Verifications().getSourceAsString();
        assertFalse(source.isEmpty());
        assertTrue(source.contains(BASH_BANG));
    }

    private void touch(File file) throws IOException {
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }

        file.setLastModified(System.currentTimeMillis());
    }
}