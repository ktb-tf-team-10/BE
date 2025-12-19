package com.ktb_tf_team_10_be.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class ResultLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 공개용 UUID (링크용)
    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(nullable = false)
    private String tempToken;

    @Column(nullable = false)
    private String jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ResultLink(String tempToken, String jobId, Invitation invitation) {
        this.uuid = UUID.randomUUID().toString();
        this.tempToken = tempToken;
        this.jobId = jobId;
        this.invitation = invitation;
        this.createdAt = LocalDateTime.now();
    }
}
