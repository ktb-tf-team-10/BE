package com.ktb_tf_team_10_be.dto;

import java.util.List;

public record ResultViewRes(
        boolean success,
        Data data
) {
    public static ResultViewRes success(
            List<String> imageUrls,
            String model3dUrl,
            Texts texts
    ) {
        return new ResultViewRes(true,
                new Data(imageUrls, model3dUrl, texts)
        );
    }

    public record Data(
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
