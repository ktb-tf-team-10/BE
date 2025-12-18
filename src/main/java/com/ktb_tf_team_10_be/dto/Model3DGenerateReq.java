package com.ktb_tf_team_10_be.dto;

import java.util.List;

public record Model3DGenerateReq(
        String mainImageUrl,           // 필수
        List<String> optionalImageUrls // 선택 (0~2)
) {

}