package com.ktb_tf_team_10_be.dto;

import java.util.List;

/**
 * FastAPI로부터 받는 2D 디자인 응답
 */
public record FastApiDesign2DResponse(
        String status,              // "success" or "failed"
        List<String> imageUrls,     // 생성된 이미지 URL 리스트
        String errorMessage         // 실패 시 에러 메시지
) {
}
