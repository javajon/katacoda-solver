package com.katacoda.solver.models;

public class CheckItemBuilder {
    private Status status = Status.Undetermined;
    private String message = "";

    public CheckItemBuilder status(Status status) {
        this.status = status;
        return this;
    }

    public CheckItemBuilder message(String message) {
        this.message = message;
        return this;
    }

    public CheckItem create() {
        return new CheckItem(status, message);
    }
}
