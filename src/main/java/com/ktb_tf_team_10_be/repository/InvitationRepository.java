package com.ktb_tf_team_10_be.repository;

import com.ktb_tf_team_10_be.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    /**
     * TEMP_TOKEN 기준 가장 최근 Invitation 1개 조회
     */
    Optional<Invitation> findTopByTempTokenOrderByCreatedAtDesc(String tempToken);
}
