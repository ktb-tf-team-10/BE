package com.ktb_tf_team_10_be.domain;

import com.ktb_tf_team_10_be.dto.InvitationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 임시 세션 식별자
    @Column(nullable = false)
    private String tempToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /* ===== 상태 전이 메서드 ===== */

    public Invitation(String tempToken, InvitationStatus status, LocalDateTime createdAt) {
        this.tempToken = tempToken;
        this.status = status;
        this.createdAt = createdAt;
    }


    public void updateStatus(InvitationStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public static Invitation createDev() {
        return new Invitation(
                "DEV-" + UUID.randomUUID(),
                InvitationStatus.INIT,
                LocalDateTime.now()
        );
    }
}
