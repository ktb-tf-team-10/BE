package com.ktb_tf_team_10_be.dto;

import java.util.List;

public record Design2DFastApiResponse(
        boolean success,
        Data data
) {
    public record Data(
            List<String> imageUrls,
            Texts texts
    ) {}

    public record Texts(
            String greeting,
            String invitation,
            String location
    ) {}
}
