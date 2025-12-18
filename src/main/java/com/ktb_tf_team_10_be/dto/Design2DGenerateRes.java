package com.ktb_tf_team_10_be.dto;

import java.util.List;

public record Design2DGenerateRes(
        String status,
        List<String> result2dImageUrls,
        Integer step,           // 현재 단계 번호 (진행 중일 때만)
        String stepName,        // 단계 이름 (진행 중일 때만)
        Integer progress        // 진행률 0-100 (진행 중일 때만)
) {
    // 완료/실패 시 사용 (progress 정보 없음)
    public Design2DGenerateRes(String status, List<String> result2dImageUrls) {
        this(status, result2dImageUrls, null, null, null);
    }
}