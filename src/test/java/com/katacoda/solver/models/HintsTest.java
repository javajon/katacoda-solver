package com.katacoda.solver.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HintsTest {

    private Hints hints = new Hints();

    @Test
    void getHint() {
        assertFalse(hints.getHint(1, 1).isEmpty());
        assertFalse(hints.getHint(1, 2).isEmpty());
        assertFalse(hints.getHint(1, 3).isEmpty());
        assertFalse(hints.getHint(1, 4).isEmpty());
        assertTrue(hints.getHint(1, 5).isEmpty());
        assertTrue(hints.getHint(99, 99).isEmpty());
    }

    @Test
    void getSource() {
        assertFalse(hints.getHintsAsString().isEmpty());
    }
}