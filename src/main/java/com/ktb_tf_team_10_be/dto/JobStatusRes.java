package com.ktb_tf_team_10_be.dto;

import java.util.List;

/**
 * FE 폴링 시 Job 상태를 반환하는 DTO
 * GET /api/jobs/{jobId}/status
 */
public record JobStatusRes(
        String jobId,
        String jobType,         // DESIGN_2D, MODEL_3D, POSTER
        String status,          // PENDING, PROCESSING, COMPLETED, FAILED
        List<String> imageUrls, // COMPLETED 시에만 값 있음
        String errorMessage     // FAILED 시에만 값 있음
) {
    /**
     * PENDING/PROCESSING 상태용 생성자
     */
    public static JobStatusRes ofProcessing(String jobId, String jobType, String status) {
        return new JobStatusRes(jobId, jobType, status, null, null);
    }

    /**
     * COMPLETED 상태용 생성자
     */
    public static JobStatusRes ofCompleted(String jobId, String jobType, List<String> imageUrls) {
        return new JobStatusRes(jobId, jobType, "COMPLETED", imageUrls, null);
    }

    /**
     * FAILED 상태용 생성자
     */
    public static JobStatusRes ofFailed(String jobId, String jobType, String errorMessage) {
        return new JobStatusRes(jobId, jobType, "FAILED", null, errorMessage);
    }
}
