package com.ktb_tf_team_10_be.dto;

import java.util.List;

public record ResultViewRes(
        boolean success,
        Data data
) {
    public static ResultViewRes success(
            String shareToken,
            String shareUrl,
            List<String> imageUrls,
            String model3dUrl,
            Texts texts
    ) {
        return new ResultViewRes(true,
                new Data(shareToken, shareUrl, imageUrls, model3dUrl, texts)
        );
    }

    public record Data(
            String shareToken,
            String shareUrl,
            List<String> imageUrls,
            String model3dUrl,
            Texts texts
    ) {}

    public record Texts(
            String greeting,
            String invitation,
            String location
    ) {}
}
