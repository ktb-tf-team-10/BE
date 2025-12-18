package com.ktb_tf_team_10_be.dto;

/**
 * FastAPI가 작업 진행 상태를 업데이트할 때 보내는 요청 DTO
 * POST /api/internal/invitations/progress?jobId=xxx
 */
public record ProgressUpdateReq(
        String jobId,      // jobId (FastAPI 측에서는 jobId 부를 수 있음)
        Integer step,       // 현재 단계 번호
        String stepName,    // 단계 이름 (GENERATING_IMAGE, GENERATING_3D, COMPLETE 등)
        Integer progress,   // 진행률 (0-100)
        String modelUrl     // 완료 시 모델 URL (step_name이 COMPLETE일 때)
) {
}
