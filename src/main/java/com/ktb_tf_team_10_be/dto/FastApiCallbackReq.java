package com.ktb_tf_team_10_be.dto;

import java.util.List;

/**
 * FastAPI가 작업 완료 후 BE에 콜백할 때 보내는 요청 DTO
 * POST /internal/jobs/{jobId}/callback
 */
public record FastApiCallbackReq(
        String status,          // "success" 또는 "failed"
        List<String> imageUrls, // 성공 시 생성된 이미지 URL 리스트
        String errorMessage     // 실패 시 에러 메시지
) {
}
