package com.ktb_tf_team_10_be.dto;

import java.util.List;

/**
 * 부분 수정 응답 DTO
 */
public record DesignEditRes(
        String status,          // PROCESSING, COMPLETED, FAILED
        List<String> imageUrls  // 수정된 이미지 URL 리스트
) {
}
