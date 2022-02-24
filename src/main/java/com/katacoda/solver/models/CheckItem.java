package com.katacoda.solver.models;

public class CheckItem {
    private final Status status;
    private final String message;

    /**
     * Messages such as from script validator that appear at end of report.
     */
    private String appendix = "";

    public CheckItem(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setAppendix(String appendix) {
        this.appendix = appendix;
    }
    public String getAppendix() {
        return appendix;
    }
}
