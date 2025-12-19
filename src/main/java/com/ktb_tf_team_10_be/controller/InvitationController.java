package com.ktb_tf_team_10_be.controller;

import com.ktb_tf_team_10_be.domain.*;
import com.ktb_tf_team_10_be.dto.*;
import com.ktb_tf_team_10_be.repository.DesignJobRepository;
import com.ktb_tf_team_10_be.repository.InvitationRepository;
import com.ktb_tf_team_10_be.repository.ResultLinkRepository;
import com.ktb_tf_team_10_be.service.Design2DFastApiClient;
//import com.ktb_tf_team_10_be.service.DesignEditFastApiClient;
import com.ktb_tf_team_10_be.service.Model3DFastApiClient;
//import com.ktb_tf_team_10_be.service.PosterFastApiClient;
import com.ktb_tf_team_10_be.service.S3Service;
import com.ktb_tf_team_10_be.service.TempTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.ktb_tf_team_10_be.domain.DesignJobType.DESIGN_2D;
import static com.ktb_tf_team_10_be.domain.DesignJobType.MODEL_3D;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InvitationController {

    private static final String TEMP_TOKEN_COOKIE = "TEMP_TOKEN";

    private final TempTokenService tempTokenService;
    private final Design2DFastApiClient design2DFastApiClient;
//    private final DesignEditFastApiClient designEditFastApiClient;
    private final Model3DFastApiClient model3DFastApiClient;
//    private final PosterFastApiClient posterFastApiClient;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    private final DesignJobRepository designJobRepository;
    private final InvitationRepository invitationRepository;
    private final ResultLinkRepository resultLinkRepository;

    private static final String DEV_TOKEN = "DEV-TOKEN-FIXED";


    @GetMapping("/api/health-check")
    public String healthCheck() {
        return "OK";
    }

    @GetMapping("/api/invitations/init")
    public ResponseEntity<InitResponse> init(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Cookie tempTokenCookie = findCookie(request, TEMP_TOKEN_COOKIE);
        if (tempTokenCookie == null) {
            String newToken = tempTokenService.issue();
            Cookie tokenCookie = createTempTokenCookie(newToken);
            response.addCookie(tokenCookie);
            Invitation  invitation = new Invitation(
                    newToken,
                    InvitationStatus.INIT,
                    LocalDateTime.now()
            );

            log.info("Invitation init: {}", invitation);
            invitationRepository.save(invitation);
        }

        InitResponse initResponse = new InitResponse(
                false,
                null,
                null,
                NextStep.START_BASIC_INFO
        );
        return ResponseEntity.ok(initResponse);
    }

    /**
     * [5] 최종 청첩장 이미지 2장 URL 요청 (동기 - 즉시 응답)
     */
    @PostMapping(
            value = "/api/invitations/design",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Design2DGenerateRes> createInvitation(
            HttpServletRequest httpRequest,
            @RequestPart("data") Design2DGenerateReq request,
            @RequestPart(value = "weddingImage", required = false)
            MultipartFile weddingImage,
            @RequestPart(value = "styleImages", required = false)
            List<MultipartFile> styleImages
    )
    {

        // 1. 쿠키에서 Invitation 조회
        Invitation invitation = getInvitationFromCookie(httpRequest);

        // 2. jobId 생성 (UUID)
        String jobId = UUID.randomUUID().toString();

        // 3. 이미지 S3 업로드
        String weddingImageUrl = s3Service.uploadImage(weddingImage, "wedding-images");
        List<String> styleImageUrls = s3Service.uploadImages(styleImages, "style-images");

        // 4. DesignJob 생성 및 저장 (PENDING 상태)
        DesignJob job = DesignJob.create(jobId, invitation, DESIGN_2D);
        designJobRepository.save(job);

        // 5. 상태를 PROCESSING으로 변경
        job.startProcessing();
        designJobRepository.save(job);

        try {

            // 5. FastAPI 2D 호출 (🔥 단 1번)
            Design2DFastApiResponse response =
                    design2DFastApiClient.requestDesign2D(
                            jobId,
                            request,
                            weddingImageUrl,
                            styleImageUrls
                    );

            Design2DFastApiResponse.Data data = response.data();

            // 6. COMPLETED + 이미지 + 텍스트 저장
            job.complete(
                    data.imageUrls(),
                    data.texts().greeting(),
                    data.texts().invitation(),
                    data.texts().location()
            );
            designJobRepository.save(job);

            // 8. FE에 완료된 이미지 URL 즉시 반환
            return ResponseEntity.ok(
                    new Design2DGenerateRes("COMPLETED", data.imageUrls())
            );

        } catch (Exception e) {
            // 실패 처리
            job.fail(e.getMessage());
            designJobRepository.save(job);

            return ResponseEntity.status(500)
                    .body(new Design2DGenerateRes("FAILED", null));
        }
    }

    /**
     * ⚠️ [DEV ONLY] 쿠키 없이 2D 디자인 테스트용 API
     * 프론트 기능 테스트 전용 (나중에 반드시 삭제)
     */
    @PostMapping(
            value = "/api/dev/invitations/design",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Design2DGenerateRes> devCreateInvitation(
            @RequestPart("request") Design2DGenerateReq request,
            @RequestPart(value = "weddingImage", required = false)
            MultipartFile weddingImage,
            @RequestPart(value = "styleImages", required = false)
            List<MultipartFile> styleImages
    ) {

        // 1️⃣ DEV Invitation (고정 토큰)
        Invitation invitation = getOrCreateDevInvitation();

        // 2️⃣ jobId
        String jobId = UUID.randomUUID().toString();

        // 3️⃣ 이미지 업로드
        String weddingImageUrl = s3Service.uploadImage(weddingImage, "wedding-images");
        List<String> styleImageUrls = s3Service.uploadImages(styleImages, "style-images");

        // 4️⃣ DesignJob 생성
        DesignJob job = DesignJob.create(jobId, invitation, DESIGN_2D);
        designJobRepository.save(job);

        // 5️⃣ PROCESSING
        job.startProcessing();
        designJobRepository.save(job);

        // 6️⃣ MOCK 결과
        List<String> mockResultImageUrls = List.of(
                weddingImageUrl,
                styleImageUrls.isEmpty() ? weddingImageUrl : styleImageUrls.get(0)
        );

        // ✅ DEV용 MOCK 텍스트
        job.complete(
                mockResultImageUrls,
                "DEV 테스트용 인사말입니다.",
                "DEV 테스트용 초대 문구입니다.",
                "DEV 테스트용 장소 문구입니다."
        );
        designJobRepository.save(job);

        // 7️⃣ 응답
        return ResponseEntity.ok(
                new Design2DGenerateRes("COMPLETED", mockResultImageUrls)
        );
    }

    /**
     * [폴링] 2D 디자인 상태 조회
     */
    @GetMapping("/api/invitations/design/status")
    public ResponseEntity<Design2DGenerateRes> getDesignStatus(HttpServletRequest httpRequest) {
        return getJobStatus(httpRequest, DESIGN_2D);
    }

    /**
     * [폴링] 3D 모델 상태 조회
     */
    @GetMapping("/api/invitations/3d/status")
    public ResponseEntity<Design2DGenerateRes> get3DStatus(HttpServletRequest httpRequest) {
        return getJobStatus(httpRequest, MODEL_3D);
    }

    /**
     * [폴링] 포스터 상태 조회
     */
    @GetMapping("/api/invitations/poster/status")
    public ResponseEntity<Design2DGenerateRes> getPosterStatus(HttpServletRequest httpRequest) {
        return getJobStatus(httpRequest, DesignJobType.POSTER);
    }

    /**
     * [6-1] 디자인 다시하기 (2D 재요청) - 청첩장 만들기로 통일
     */
    @Deprecated
    @PostMapping(
            value = "/api/invitations/design/retry",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Design2DGenerateRes> retryDesign(
            HttpServletRequest httpRequest,
            @RequestPart("data") Design2DGenerateReq request,
            @RequestPart(value = "weddingImage", required = false)
            MultipartFile weddingImage,
            @RequestPart(value = "styleImages", required = false)
            List<MultipartFile> styleImages
    ) {
        // 디자인 다시하기도 새로운 Job 생성 (기존과 동일한 로직)
        return createInvitation(httpRequest, request, weddingImage, styleImages);
    }

    /**
     * [6-2] 부분 수정하기
     */
//    @PostMapping(
//            value = "/api/invitations/design/edit",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public ResponseEntity<DesignEditRes> editDesign(
//            HttpServletRequest httpRequest,
//            @RequestPart("request") DesignEditReq request,
//            @RequestPart(value = "styleImages", required = false)
//            List<MultipartFile> styleImages
//    ) {
//        // 1. 쿠키에서 Invitation 조회
//        Invitation invitation = getInvitationFromCookie(httpRequest);
//        if (invitation == null) {
//            return ResponseEntity.status(401).body(new DesignEditRes("NO_COOKIE", null));
//        }
//
//        // 2. jobId 생성
//        String jobId = UUID.randomUUID().toString();
//
//        // 3. 스타일 이미지 S3 업로드
//        List<String> styleImageUrls = s3Service.uploadImages(styleImages, "style-images");
//
//        // 4. DesignJob 생성 (DESIGN_2D - 부분 수정도 2D 카테고리)
//        DesignJob job = DesignJob.create(jobId, invitation, DesignJobType.DESIGN_2D);
//        designJobRepository.save(job);
//
//        // 5. PROCESSING으로 변경
//        job.startProcessing();
//        designJobRepository.save(job);
//
//        try {
//            // 6. FastAPI 호출 (동기 - 이미지 URL 전달)
//            List<String> resultImageUrls = designEditFastApiClient.requestEdit(jobId, request, styleImageUrls);
//
//            // 7. Job 완료 처리
//            job.complete(resultImageUrls);
//            designJobRepository.save(job);
//
//            // 8. FE에 완료된 이미지 URL 즉시 반환
//            return ResponseEntity.ok(new DesignEditRes("COMPLETED", resultImageUrls));
//
//        } catch (Exception e) {
//            // 실패 처리
//            job.fail(e.getMessage());
//            designJobRepository.save(job);
//
//            return ResponseEntity.status(500)
//                    .body(new DesignEditRes("FAILED", null));
//        }
//    }

    /**
     * [7-3] 3D 청첩장 요청
     */
    @PostMapping("/api/invitations/3d")
    public ResponseEntity<Design2DGenerateRes> create3DModel(
            HttpServletRequest httpRequest,
            @RequestPart("mainImage")
            MultipartFile mainImage,
            @RequestPart(value = "optionalImages", required = false)
            List<MultipartFile> optionalImages
    ) {
        Invitation invitation = getInvitationFromCookie(httpRequest);
        if (invitation == null) {
            return ResponseEntity.status(401).body(new Design2DGenerateRes("NO_COOKIE", null));
        }

        // 1. jobId 생성
        String jobId = UUID.randomUUID().toString();

        // 2. DesignJob 생성 (MODEL_3D)
        DesignJob job = DesignJob.create(jobId, invitation, MODEL_3D);
        designJobRepository.save(job);

        // 3️⃣ S3 업로드
        String imageUrl1 = s3Service.uploadImage(mainImage, "main-images");

        List<String> optionalImageUrls = optionalImages != null
                ? s3Service.uploadImages(optionalImages, "optional-images")
                : List.of();
        String imageUrl2 = optionalImageUrls.size() > 0 ? optionalImageUrls.get(0) : null;
        String imageUrl3 = optionalImageUrls.size() > 1 ? optionalImageUrls.get(1) : null;

        // 3. FastAPI 호출 (비동기)
        model3DFastApiClient.request3D(jobId, imageUrl1, imageUrl2, imageUrl3);

        // 4. PROCESSING으로 변경
        job.startProcessing();
        designJobRepository.save(job);

        return ResponseEntity.ok(new Design2DGenerateRes("PROCESSING", null));
    }

//    /**
//     * [8] 포스터 요청
//     */
//    @PostMapping("/api/invitations/poster")
//    public ResponseEntity<Design2DGenerateRes> createPoster(HttpServletRequest httpRequest) {
//        Invitation invitation = getInvitationFromCookie(httpRequest);
//        if (invitation == null) {
//            return ResponseEntity.status(401).body(new Design2DGenerateRes("NO_COOKIE", null));
//        }
//
//        // 1. jobId 생성
//        String jobId = UUID.randomUUID().toString();
//
//        // 2. DesignJob 생성 (POSTER)
//        DesignJob job = DesignJob.create(jobId, invitation, DesignJobType.POSTER);
//        designJobRepository.save(job);
//
//        // 3. FastAPI 호출 (비동기)
//        posterFastApiClient.requestPoster(jobId);
//
//        // 4. PROCESSING으로 변경
//        job.startProcessing();
//        designJobRepository.save(job);
//
//        return ResponseEntity.ok(new Design2DGenerateRes("PROCESSING", null));
//    }

    @GetMapping("/api/results")
    public ResponseEntity<ResultViewRes> getResult(HttpServletRequest request) {
        // 1. 쿠키에서 Invitation 조회
        Invitation invitation = getInvitationFromCookie(request);
        if (invitation == null) {
            return ResponseEntity.status(401).body(new ResultViewRes(false, null));
        }

        List<DesignJob> jobs = designJobRepository.findByInvitationId(invitation.getId());
        for (DesignJob job : jobs) {
            log.info(job.toString());
        }
        // 2. JobType으로 최신 Job 조회
        DesignJob job_2d = designJobRepository
                .findTopByInvitationIdAndJobTypeOrderByCreatedAtDesc(invitation.getId(), DESIGN_2D)
                .orElse(null);

        DesignJob job_3d = designJobRepository
                .findTopByInvitationIdAndJobTypeOrderByCreatedAtDesc(invitation.getId(), MODEL_3D)
                .orElse(null);

        if (job_2d == null || job_3d == null) {
            return ResponseEntity.status(404).body(new ResultViewRes(false, null));
        }

        return ResponseEntity.ok(
                ResultViewRes.success(
                        job_2d.getResultImageUrls(),
                        job_3d.getResultImageUrls().get(0),
                        new ResultViewRes.Texts(
                                job_2d.getText_greeting(),
                                job_2d.getText_invitation(),
                                job_2d.getText_location()
                        )
                )
        );
    }

    /**
     * ⚠️ [DEV ONLY] 쿠키 없이 3D 모델 테스트용 API
     * FastAPI 호출 없이 DB에 바로 COMPLETED 저장
     */
    @PostMapping(
            value = "/api/dev/invitations/3d",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Design2DGenerateRes> devCreate3D(
            @RequestPart("mainImage") MultipartFile mainImage,
            @RequestPart(value = "optionalImages", required = false)
            List<MultipartFile> optionalImages
    ) {
        // 1️⃣ DEV Invitation 생성
        Invitation invitation = getOrCreateDevInvitation();

        // 2️⃣ jobId 생성
        String jobId = UUID.randomUUID().toString();

        // 3️⃣ DesignJob 생성 (MODEL_3D)
        DesignJob job = DesignJob.create(jobId, invitation, MODEL_3D);
        designJobRepository.save(job);

        // 4️⃣ S3 업로드
        String mainImageUrl = s3Service.uploadImage(mainImage, "dev-3d/main");

        List<String> optionalUrls = optionalImages != null
                ? s3Service.uploadImages(optionalImages, "dev-3d/optional")
                : List.of();

        // 5️⃣ 🎯 MOCK 3D 결과 URL
        String mock3dModelUrl =
                "https://dns7warjxrmv9.cloudfront.net/3d_models/dev-" + jobId + ".glb";

        // 6️⃣ 바로 COMPLETED 처리
        job.startProcessing();
        job.complete(List.of(mock3dModelUrl));
        designJobRepository.save(job);

        // 7️⃣ 응답
        return ResponseEntity.ok(
                new Design2DGenerateRes(
                        "COMPLETED",
                        List.of(mock3dModelUrl)
                )
        );
    }
    // token이랑 taskId로 기록된 값을 보여주는

    /* ========== 공통 헬퍼 메서드 ========== */

    /**
     * 쿠키에서 Invitation 조회
     */
    private Invitation getInvitationFromCookie(HttpServletRequest request) {
        Cookie tempTokenCookie = findCookie(request, TEMP_TOKEN_COOKIE);
        if (tempTokenCookie == null) {
            return null;
        }
        return invitationRepository
                .findTopByTempTokenOrderByCreatedAtDesc(tempTokenCookie.getValue())
                .orElse(null);
    }

    /**
     * 공통 Job 상태 조회 로직
     */
    private ResponseEntity<Design2DGenerateRes> getJobStatus(HttpServletRequest httpRequest, DesignJobType jobType) {
        // 1. 쿠키에서 Invitation 조회
        Invitation invitation = getInvitationFromCookie(httpRequest);
        if (invitation == null) {
            return ResponseEntity.status(401).body(new Design2DGenerateRes("NO_COOKIE", null));
        }

        // 2. JobType으로 최신 Job 조회
        DesignJob job = designJobRepository
                .findTopByInvitationIdAndJobTypeOrderByCreatedAtDesc(invitation.getId(), jobType)
                .orElse(null);

        if (job == null) {
            return ResponseEntity.status(404).body(new Design2DGenerateRes("NO_JOB", null));
        }

        // 3. Job 상태에 따라 응답
        return switch (job.getStatus()) {
            case PENDING, PROCESSING -> ResponseEntity.ok(
                    new Design2DGenerateRes(
                            "PROCESSING",
                            null,
                            job.getStep(),
                            job.getStepName(),
                            job.getProgress()
                    )
            );
            case COMPLETED -> ResponseEntity.ok(new Design2DGenerateRes("COMPLETED", job.getResultImageUrls()));
            case FAILED -> ResponseEntity.ok(new Design2DGenerateRes("FAILED", null));
        };
    }

    private Cookie createTempTokenCookie(String token) {

        Cookie cookie = new Cookie(TEMP_TOKEN_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // local=false
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }


    private Cookie findCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }
    private Invitation getOrCreateDevInvitation() {
        return invitationRepository
                .findTopByTempTokenOrderByCreatedAtDesc(DEV_TOKEN)
                .orElseGet(() -> {
                    Invitation inv = new Invitation(
                            DEV_TOKEN,
                            InvitationStatus.INIT,
                            LocalDateTime.now()
                    );
                    return invitationRepository.save(inv);
                });
    }
}
