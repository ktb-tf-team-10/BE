package com.ktb_tf_team_10_be.dto;


import java.util.Map;

/**
 * FastAPI로부터 받는 2D 디자인 응답
 */
public record FastApiDesign2DResponse(
        boolean success,
        Data data
) {
    public record Data(
            java.util.List<String> imageUrls,
            Map<String, String> texts
    ) {}
}
