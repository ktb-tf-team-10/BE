package com.ktb_tf_team_10_be.domain;


import com.ktb_tf_team_10_be.dto.DesignJobStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DesignJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FE에 전달할 고유 ID
    @Column(nullable = false, unique = true, length = 50)
    private String jobId;

    // Invitation과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id")
    private Invitation invitation;

    // 작업 타입 (2D, 3D, POSTER)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DesignJobType jobType;

    // 작업 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DesignJobStatus status;

    // 완성된 이미지 URL 리스트 (JSON 배열로 저장)
    @ElementCollection
    @CollectionTable(name = "design_job_result_urls", joinColumns = @JoinColumn(name = "design_job_id"))
    @Column(name = "image_url", length = 500)
    private List<String> resultImageUrls = new ArrayList<>();

    // 에러 메시지 (실패 시)
    @Column(length = 1000)
    private String errorMessage;

    // 진행 상태 추적 (3D, Poster 등 긴 작업용)
    private Integer step;           // 현재 단계 번호

    @Column(length = 50)
    private String stepName;        // 현재 단계 이름 (GENERATING_IMAGE, GENERATING_3D, COMPLETE 등)

    private Integer progress;       // 진행률 (0-100)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /* ===== 생성 메서드 ===== */

    public static DesignJob create(String jobId, Invitation invitation, DesignJobType jobType) {
        DesignJob job = new DesignJob();
        job.jobId = jobId;
        job.invitation = invitation;
        job.jobType = jobType;
        job.status = DesignJobStatus.PENDING;
        job.createdAt = LocalDateTime.now();
        job.updatedAt = LocalDateTime.now();
        return job;
    }

    /* ===== 상태 변경 메서드 ===== */

    public void startProcessing() {
        this.status = DesignJobStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProgress(Integer step, String stepName, Integer progress) {
        this.step = step;
        this.stepName = stepName;
        this.progress = progress;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(List<String> imageUrls) {
        this.status = DesignJobStatus.COMPLETED;
        this.resultImageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = DesignJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
}