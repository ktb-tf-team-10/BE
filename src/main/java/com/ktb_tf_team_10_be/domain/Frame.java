package com.ktb_tf_team_10_be.domain;

public enum Frame {

    CLASSIC("클래식", "elegant classic wedding invitation"),
    FLORAL("플로럴", "flower-rich floral wedding invitation"),
    MINIMAL("미니멀", "clean minimal wedding invitation"),
    ROMANTIC("로맨틱", "soft romantic wedding invitation");

    private final String displayName;
    private final String promptHint;

    Frame(String displayName, String promptHint) {
        this.displayName = displayName;
        this.promptHint = promptHint;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPromptHint() {
        return promptHint;
    }
}