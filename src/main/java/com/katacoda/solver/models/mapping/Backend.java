package com.katacoda.solver.models.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Backend {
    String imageid = "";

    public String getImageid() {
        return imageid;
    }
}
