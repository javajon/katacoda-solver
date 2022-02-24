package com.katacoda.solver.models.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Host {
    private AssetFile[] assets = new AssetFile[0];

    public AssetFile[] getAssets() {
        return assets;
    }
}
