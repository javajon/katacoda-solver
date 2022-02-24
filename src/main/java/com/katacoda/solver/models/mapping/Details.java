package com.katacoda.solver.models.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Details {
    private Page intro = new Page();
    private Page finish = new Page();
    private Page[] steps = { };

    private Assets assets = new Assets();

    public Page getIntro() {
        return intro;
    }

    public Page getFinish() {
        return finish;
    }

    public Assets getAssets() {
        return assets;
    }

    public Page[] getSteps() {
        return steps;
    }
}
