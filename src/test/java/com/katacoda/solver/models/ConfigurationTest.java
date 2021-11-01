package com.katacoda.solver.models;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void resetCurrentTask() {
        Configuration.resetCurrentTask();
        assertEquals(Configuration.getCurrentTask(), 1);
    }

    @Test
    void getHintsEnabled() {
        Configuration.setHintEnabled(true);
        assertTrue(Configuration.getHintsEnabled());
    }

    @Test
    void getCurrentTask() {
        Configuration.setCurrentTask(2);
        assertEquals(Configuration.getCurrentTask(), 2);
    }

    @Test
    void isChallengeComplete() {
        Configuration.setCurrentTask(1);
        assertFalse(Configuration.isChallengeComplete());
        Configuration.setCurrentTask(0);
        assertTrue(Configuration.isChallengeComplete());
    }

    @AfterAll
    static void afterAll() {
        Configuration.resetCurrentTask();
    }
}
