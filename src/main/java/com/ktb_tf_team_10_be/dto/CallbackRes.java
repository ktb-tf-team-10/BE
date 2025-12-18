package com.ktb_tf_team_10_be.dto;

/**
 * FastAPI 콜백 응답 DTO
 */
public record CallbackRes(
        String message,  // "success" 또는 에러 메시지
        String jobId     // Job ID
) {
    public static CallbackRes success(String jobId) {
        return new CallbackRes("Callback processed successfully", jobId);
    }

    public static CallbackRes error(String jobId, String errorMessage) {
        return new CallbackRes(errorMessage, jobId);
    }
}
