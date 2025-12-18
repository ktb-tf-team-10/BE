package com.ktb_tf_team_10_be.dto;


/**
 * FastAPI로부터 받는 2D 디자인 응답
 */
public record FastApiDesign2DResponse(
        boolean success,
        Data data
) {
    public record Data(
            java.util.List<String> imageUrls,
            Texts texts
    ) {}

    public record Texts(
            String greeting,
            String invitation,
            String location,
            String closing,
            String extraMessage,
            String additionalRequest
    ) {}
}
