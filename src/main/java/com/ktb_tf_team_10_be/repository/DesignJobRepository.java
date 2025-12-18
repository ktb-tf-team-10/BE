package com.ktb_tf_team_10_be.repository;

import com.ktb_tf_team_10_be.domain.DesignJob;
import com.ktb_tf_team_10_be.domain.DesignJobType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DesignJobRepository extends JpaRepository<DesignJob, Long> {

    /**
     * jobId로 Job 조회 (내부 콜백 시 사용)
     */
    Optional<DesignJob> findByJobId(String jobId);

    /**
     * Invitation에 속한 모든 Job 조회
     */
    List<DesignJob> findByInvitationId(Long invitationId);

    /**
     * Invitation + JobType으로 최신 Job 조회 (FE 폴링 시 사용)
     * 쿠키 → Invitation → 특정 타입의 최신 Job
     */
    Optional<DesignJob> findTopByInvitationIdAndJobTypeOrderByCreatedAtDesc(Long invitationId, DesignJobType jobType);
}
