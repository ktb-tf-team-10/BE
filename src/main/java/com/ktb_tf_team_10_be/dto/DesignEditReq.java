package com.ktb_tf_team_10_be.dto;

import java.util.List;

/**
 * 부분 수정 요청 DTO
 */
public record DesignEditReq(
        List<String> imageUrls,    // 편집할 이미지 URL
        String editText     // 편집 요청 텍스트 (예: "글자 크기 키워줘")
) {
}
