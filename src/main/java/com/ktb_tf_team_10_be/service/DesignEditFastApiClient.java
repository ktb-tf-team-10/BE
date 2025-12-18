package com.ktb_tf_team_10_be.service;

import com.ktb_tf_team_10_be.dto.DesignEditReq;
import com.ktb_tf_team_10_be.dto.FastApiDesign2DResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignEditFastApiClient {

    private final WebClient fastApiClient;
    private final ObjectMapper objectMapper;

    /**
     * 부분 수정 요청 (동기 - 응답 대기)
     * @param jobId Job ID
     * @param request 수정 요청 정보 (imageUrl, editText)
     * @param styleImageUrls S3에 업로드된 스타일 이미지 URL 리스트
     * @return 수정된 이미지 URL 리스트
     */
    public List<String> requestEdit(
            String jobId,
            DesignEditReq request,
            List<String> styleImageUrls
    ) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // jobId
        bodyBuilder.part("jobId", jobId);

        // request (imageUrl, editText)
        bodyBuilder.part("request", toJson(request))
                .contentType(MediaType.APPLICATION_JSON);

        // styleImageUrls (JSON 배열)
        if (styleImageUrls != null && !styleImageUrls.isEmpty()) {
            bodyBuilder.part("styleImageUrls", toJson(styleImageUrls))
                    .contentType(MediaType.APPLICATION_JSON);
        }

        try {
            // FastAPI 호출 및 응답 대기
            FastApiDesign2DResponse response = fastApiClient.post()
                    .uri("/internal/design/edit")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(bodyBuilder.build())
                    .retrieve()
                    .bodyToMono(FastApiDesign2DResponse.class)
                    .block();

            if (response != null && "success".equals(response.status())) {
                return response.imageUrls() != null ? response.imageUrls() : Collections.emptyList();
            } else {
                String error = response != null ? response.errorMessage() : "Unknown error";
                throw new RuntimeException("FastAPI 부분 수정 실패: " + error);
            }
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 호출 실패: " + e.getMessage(), e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}


