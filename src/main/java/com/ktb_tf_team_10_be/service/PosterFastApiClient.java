//package com.ktb_tf_team_10_be.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.MultipartBodyBuilder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Service
//@RequiredArgsConstructor
//public class PosterFastApiClient {
//
//    private final WebClient fastApiClient;
//
//    /**
//     * 포스터 생성 요청 (비동기 - 콜백으로 결과 수신)
//     * @param jobId Job ID
//     */
//    public void requestPoster(String jobId) {
//        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
//
//        // jobId
//        bodyBuilder.part("jobId", jobId);
//
//        // TODO: 포스터 생성에 필요한 추가 파라미터가 있다면 여기에 추가
//        // 예: 청첩장 이미지 URL, 텍스트 정보 등
//
//        try {
//            // FastAPI에 비동기 요청 (응답을 기다리지 않음)
//            fastApiClient.post()
//                    .uri("/internal/design/poster")
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .bodyValue(bodyBuilder.build())
//                    .retrieve()
//                    .toBodilessEntity()
//                    .subscribe(
//                            response -> {
//                                // 요청 성공 (로그만 남김)
//                                System.out.println("[Poster] FastAPI 요청 성공: jobId=" + jobId);
//                            },
//                            error -> {
//                                // 요청 실패
//                                System.err.println("[Poster] FastAPI 요청 실패: " + error.getMessage());
//                            }
//                    );
//        } catch (Exception e) {
//            throw new RuntimeException("FastAPI 포스터 요청 실패: " + e.getMessage(), e);
//        }
//    }
//}
