package com.ktb_tf_team_10_be.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktb_tf_team_10_be.dto.Design2DFastApiResponse;
import com.ktb_tf_team_10_be.dto.Design2DGenerateReq;
import com.ktb_tf_team_10_be.dto.FastApiDesign2DRequest;
import com.ktb_tf_team_10_be.dto.FastApiDesign2DResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Design2DFastApiClient {

    private final WebClient fastApi2DClient;
    private final ObjectMapper objectMapper;

    /**
     * 2D 디자인 요청 (동기 - 응답을 기다림)
     * @param jobId Job ID
     * @param req 디자인 요청 정보
     * @param weddingImageUrl S3에 업로드된 웨딩 이미지 URL
     * @param styleImageUrls S3에 업로드된 스타일 이미지 URL 리스트
     * @return 생성된 이미지 URL 리스트
     */
    public Design2DFastApiResponse requestDesign2D(
            String jobId,
            Design2DGenerateReq req,
            String weddingImageUrl,
            List<String> styleImageUrls
    ) {
        FastApiDesign2DRequest body = new FastApiDesign2DRequest(
                req.groom(),
                req.bride(),
                req.wedding(),
                weddingImageUrl,
                styleImageUrls != null && !styleImageUrls.isEmpty()
                        ? styleImageUrls.get(0)
                        : null,
                req.extraMessage(),
                req.additionalRequest(),
                req.tone()
        );

        Design2DFastApiResponse response = fastApi2DClient.post()
                .uri("/api/generate-invitation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Design2DFastApiResponse.class)
                .block();

        if (response == null || !response.success() || response.data() == null) {
            throw new RuntimeException("FastAPI 2D 디자인 실패");
        }

        return response;
    }
}