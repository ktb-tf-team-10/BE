package com.ktb_tf_team_10_be.repository;

import com.ktb_tf_team_10_be.domain.ResultLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultLinkRepository extends JpaRepository<ResultLink, Long> {
    Optional<ResultLink> findByUuid(String uuid);
}
