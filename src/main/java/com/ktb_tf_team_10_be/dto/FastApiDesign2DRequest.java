package com.ktb_tf_team_10_be.dto;

public record FastApiDesign2DRequest(
        Design2DGenerateReq.Groom groom,
        Design2DGenerateReq.Bride bride,
        Design2DGenerateReq.Wedding wedding,
        String weddingImageUrl,
        String styleImageUrl,
        String extraMessage,
        String additionalRequest,
        String tone,
        String frame, // 삭제해야함
        String modelName
) {}