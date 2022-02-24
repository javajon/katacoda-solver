package com.katacoda.solver.models.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioIndex {
    private String type = "";
    private String title = "";
    private String description = "";
    private String time = "";
    private String difficulty = "";
    private Details details = new Details();
    private Backend backend = new Backend();

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Backend getBackend() {
        return backend;
    }

    public Details getDetails() {
        return details;
    }
}
