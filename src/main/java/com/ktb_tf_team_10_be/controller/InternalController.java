package com.ktb_tf_team_10_be.controller;

import com.ktb_tf_team_10_be.domain.DesignJob;
import com.ktb_tf_team_10_be.domain.Invitation;
import com.ktb_tf_team_10_be.domain.ResultLink;
import com.ktb_tf_team_10_be.dto.CallbackRes;
import com.ktb_tf_team_10_be.dto.FastApiCallbackReq;
import com.ktb_tf_team_10_be.dto.ProgressUpdateReq;
import com.ktb_tf_team_10_be.repository.DesignJobRepository;
import com.ktb_tf_team_10_be.repository.InvitationRepository;
import com.ktb_tf_team_10_be.repository.ResultLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * FastAPI 내부 통신용 컨트롤러
 * 외부 노출 금지 (보안 설정 필요)
 */
@Slf4j
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final DesignJobRepository designJobRepository;
    private final ResultLinkRepository resultLinkRepository;
    private final InvitationRepository invitationRepository;

    /**
     * [진행 상태 업데이트] FastAPI가 작업 진행 상태를 주기적으로 업데이트
     * POST /api/internal/invitations/progress
     * Body: {"jobId": "xxx", "step": 1, "step_name": "GENERATING_IMAGE", "progress": 15}
     */
    @PostMapping("/invitations/progress")
    public ResponseEntity<CallbackRes> updateProgress(
            @RequestBody ProgressUpdateReq request
    ) {

        // 1. jobId로 DesignJob 조회
        DesignJob job = designJobRepository.findByJobId(request.jobId())
                .orElse(null);

        if (job == null) {
            log.error("[Progress] Job not found: jobId={}", request.jobId());
            return ResponseEntity.status(404)
                    .body(CallbackRes.error(request.jobId(), "Job not found"));
        }

        // 2. 진행 상태 업데이트
        job.updateProgress(request.step(), request.stepName(), request.progress());
        designJobRepository.save(job);

        // 3. COMPLETE 상태면서 modelUrl이 있으면 완료 처리
        if ("COMPLETE".equals(request.stepName()) && request.modelUrl() != null) {
            job.complete(java.util.List.of(request.modelUrl()));
            designJobRepository.save(job);

            Invitation invitation = job.getInvitation();

            ResultLink resultLink = new ResultLink(
                    invitation.getTempToken(),
                    job.getJobId(),
                    invitation
            );

            resultLinkRepository.save(resultLink);
            log.info("[Progress] Job marked as COMPLETED: jobId={}, modelUrl={}", request.jobId(), request.modelUrl());
        }

        // 4. 성공 응답
        return ResponseEntity.ok(CallbackRes.success(request.jobId()));
    }

    /**
     * [7-3-1] FastAPI 작업 완료 콜백
     * FastAPI가 작업 완료 시 이 엔드포인트를 호출하여 결과를 전달
     */
    @PostMapping("/invitations/callback")
    public ResponseEntity<CallbackRes> handleCallback(
            @RequestBody FastApiCallbackReq request,
            @RequestParam String jobId
    ) {
        log.info("[Callback] Received callback for jobId={}, status={}", jobId, request.status());

        // 1. jobId로 DesignJob 조회
        DesignJob job = designJobRepository.findByJobId(jobId)
                .orElse(null);

        if (job == null) {
            log.error("[Callback] Job not found: jobId={}", jobId);
            return ResponseEntity.status(404)
                    .body(CallbackRes.error(jobId, "Job not found"));
        }

        // 2. 상태에 따라 Job 업데이트
        if ("success".equals(request.status())) {
            // 성공: COMPLETED 상태로 변경, imageUrls 저장
            job.complete(request.imageUrls());
            designJobRepository.save(job);
            log.info("[Callback] Job completed: jobId={}, imageCount={}",
                    jobId, request.imageUrls() != null ? request.imageUrls().size() : 0);

        } else if ("failed".equals(request.status())) {
            // 실패: FAILED 상태로 변경, 에러 메시지 저장
            job.fail(request.errorMessage());
            designJobRepository.save(job);
            log.error("[Callback] Job failed: jobId={}, error={}", jobId, request.errorMessage());

        } else {
            log.error("[Callback] Unknown status: jobId={}, status={}", jobId, request.status());
            return ResponseEntity.status(400)
                    .body(CallbackRes.error(jobId, "Unknown status: " + request.status()));
        }

        // 3. 성공 응답
        return ResponseEntity.ok(CallbackRes.success(jobId));
    }
}
