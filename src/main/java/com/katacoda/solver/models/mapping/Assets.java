package com.katacoda.solver.models.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Assets {
    private AssetFile[] host01 = { };

    public AssetFile[] getHost01() {
        return host01;
    }
}
