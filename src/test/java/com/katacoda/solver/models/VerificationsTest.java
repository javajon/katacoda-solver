package com.katacoda.solver.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificationsTest {

    private static final String BASH_BANG = "#!/bin/bash";

    @Test
    void requestTaskAdvance() {
        Configuration.setCurrentTask(1);

        assertTrue(new Verifications().requestTaskAdvance(Configuration.getCurrentTask()));
        assertTrue(new Verifications().requestTaskAdvance(Configuration.getCurrentTask()));
        assertTrue(new Verifications().requestTaskAdvance(Configuration.getCurrentTask()));
        assertTrue(Configuration.isChallengeComplete());

        Configuration.resetCurrentTask();
    }

    @Test
    void getSource() {
        String source = new Verifications().getSourceAsString();
        assertFalse(source.isEmpty());
        assertTrue(source.contains(BASH_BANG));
    }
}
