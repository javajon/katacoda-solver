package com.katacoda.solver.models.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {
    private String text = "";
    private String courseData = "";
    private String code = "";
    private String title = "";
    private String verify = "";
    private String hint = "";

    public String getText() {
        return text;
    }

    public String getCourseData() {
        return courseData;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getVerify() {
        return verify;
    }

    public String getHint() {
        return hint;
    }
}
