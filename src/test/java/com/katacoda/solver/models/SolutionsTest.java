package com.katacoda.solver.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolutionsTest {

    private static final String BASH_BANG = "#!/bin/bash";

    @Test
    void exists() {
        assertTrue(new Solutions().exists(1));
        assertTrue(new Solutions().exists(2));
        assertFalse(new Solutions().exists(0));
        assertFalse(new Solutions().exists(99));
    }

    @Test
    void solve() {
//        new Solutions().solve();
    }

    @Test
    void getSource() {
        String source = new Solutions().getSourceAsString();
        assertFalse(source.isEmpty());
        assertTrue(source.contains(BASH_BANG));
    }
}