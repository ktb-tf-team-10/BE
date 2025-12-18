package com.ktb_tf_team_10_be.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktb_tf_team_10_be.dto.Design2DGenerateReq;
import com.ktb_tf_team_10_be.dto.FastApiDesign2DResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Design2DFastApiClient {

    private final WebClient fastApiClient;
    private final ObjectMapper objectMapper;

    /**
     * 2D 디자인 요청 (동기 - 응답을 기다림)
     * @param jobId Job ID
     * @param request 디자인 요청 정보
     * @param weddingImageUrl S3에 업로드된 웨딩 이미지 URL
     * @param styleImageUrls S3에 업로드된 스타일 이미지 URL 리스트
     * @return 생성된 이미지 URL 리스트
     */
    public List<String> requestDesign2D(
            String jobId,
            Design2DGenerateReq request,
            String weddingImageUrl,
            List<String> styleImageUrls
    ) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // jobId
        bodyBuilder.part("jobId", jobId);

        // request (JSON)
        bodyBuilder.part("request", toJson(request))
                .contentType(MediaType.APPLICATION_JSON);

        // weddingImageUrl
        if (weddingImageUrl != null) {
            bodyBuilder.part("weddingImageUrl", weddingImageUrl);
        }

        // styleImageUrls (JSON 배열)
        if (styleImageUrls != null && !styleImageUrls.isEmpty()) {
            bodyBuilder.part("styleImageUrls", toJson(styleImageUrls))
                    .contentType(MediaType.APPLICATION_JSON);
        }

        try {
            // FastAPI 호출 및 응답 대기 (동기)
            FastApiDesign2DResponse response = fastApiClient.post()
                    .uri("/internal/design/2d")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(bodyBuilder.build())
                    .retrieve()
                    .bodyToMono(FastApiDesign2DResponse.class)
                    .block();

            // 응답 처리
            if (response != null && "success".equals(response.status())) {
                return response.imageUrls() != null ? response.imageUrls() : Collections.emptyList();
            } else {
                String error = response != null ? response.errorMessage() : "Unknown error";
                throw new RuntimeException("FastAPI 2D 디자인 생성 실패: " + error);
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