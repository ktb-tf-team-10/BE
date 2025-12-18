package com.ktb_tf_team_10_be.dto;

public enum InvitationStatus {

    // 초기
    CREATED,                // invitation 생성 직후
    BASIC_INFO_DONE,        // STEP 2 완료

    // 멘트/문구
    TONE_SELECTED,          // STEP 3
    MESSAGE_DONE,           // STEP 4

    // 이미지/디자인
    IMAGE_UPLOADED,         // STEP 5
    DESIGN_PROCESSING,      // STEP 6 (FastAPI 작업 중)
    DESIGN_DONE,            // 2D 결과 완료

    // 특별 기능
    FEATURE_SELECTED,       // STEP 8

    // 3D
    MODEL_3D_PROCESSING,    // STEP 9
    MODEL_3D_DONE,          // 3D 완료

    // 포스터
    POSTER_PROCESSING,      // STEP 10
    POSTER_DONE,            // 포스터 완료

    // 최종
    COMPLETED,              // STEP 11 전체 완료
    FAILED                  // 실패
}