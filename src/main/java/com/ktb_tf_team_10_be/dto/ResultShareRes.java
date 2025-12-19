package com.ktb_tf_team_10_be.dto;

public record ResultShareRes(
        boolean success,
        Data data
) {
    public static ResultShareRes success(String shareToken, String shareUrl) {
        return new ResultShareRes(true, new Data(shareToken, shareUrl));
    }

    public record Data(
            String shareToken,
            String shareUrl
    ) {}
}
