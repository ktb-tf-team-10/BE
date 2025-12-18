package com.ktb_tf_team_10_be.dto;

/**
 * FE에 jobId를 반환하기 위한 응답 DTO
 * 2D 디자인, 3D 모델, 포스터 요청 시 사용
 */
public record JobResponse(
        String jobId
) {
}
