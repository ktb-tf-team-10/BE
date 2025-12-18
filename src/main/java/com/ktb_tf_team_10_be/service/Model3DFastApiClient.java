package com.ktb_tf_team_10_be.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktb_tf_team_10_be.dto.Model3DFastApiGenerateReq;
import com.ktb_tf_team_10_be.dto.Model3DGenerateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Model3DFastApiClient {

    private final WebClient fastApiClient;
    private final ObjectMapper objectMapper;

    /**
     * 3D 모델 생성 요청 (비동기 - 콜백으로 결과 수신)
     * @param jobId Job ID
     */
    public void request3D(
            String jobId,
            String imageUrl1,
            String imageUrl2,
            String imageUrl3

    ) {

        Model3DFastApiGenerateReq request = new Model3DFastApiGenerateReq(
                jobId,
                imageUrl1,
                imageUrl2,
                imageUrl3
        );

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // jobId
        bodyBuilder.part("jobId", jobId);
        bodyBuilder.part("imageUrl1", imageUrl1);
        bodyBuilder.part("imageUrl2", imageUrl2);
        bodyBuilder.part("imageUrl3", imageUrl3);

        // TODO: 3D 생성에 필요한 추가 파라미터가 있다면 여기에 추가
        // 예: 2D 이미지 URL, 설정값 등

        try {
            // FastAPI에 비동기 요청 (응답을 기다리지 않음)
            fastApiClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe(
                            response -> {
                                // 요청 성공 (로그만 남김)
                                System.out.println("[3D] FastAPI 요청 성공: jobId=" + jobId);
                            },
                            error -> {
                                // 요청 실패
                                System.err.println("[3D] FastAPI 요청 실패: " + error.getMessage());
                            }
                    );
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 3D 요청 실패: " + e.getMessage(), e);
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
