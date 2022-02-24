package com.katacoda.solver.models.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetFile {
    private String file = "";
    private String target = "";
    private String chmod = "";

    public String getChmod() {
        return chmod;
    }

    public String getTarget() {
        return target;
    }

    public String getFile() {
        return file;
    }
}
