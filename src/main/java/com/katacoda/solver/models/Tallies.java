package com.katacoda.solver.models;

public class Tallies {
    private int pass;
    private int warning;
    private int error;

    public void add(Status status) {
        switch (status) {
            case Pass:
                pass++;
                break;
            case Warning:
                warning++;
                break;
            case Error:
                error++;
                break;
        }
    }

    public String report() {
        return String.format("Passes: %d,   Warnings: %d,   Errors: %d", pass, warning, error);
    }

    public boolean hasErrors() {
        return error > 0;
    }
}
