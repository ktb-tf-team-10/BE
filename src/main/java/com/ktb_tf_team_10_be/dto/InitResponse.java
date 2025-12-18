package com.ktb_tf_team_10_be.dto;

import com.ktb_tf_team_10_be.domain.NextStep;

public record InitResponse(
        boolean hasInvitation,
        String invitationId,
        InvitationStatus status,
        NextStep nextStep
) {
    public static InitResponse noInvitation() {
        return new InitResponse(
                false,                 // hasInvitation
                null,                  // invitationId
                null,                  // status
                NextStep.START_BASIC_INFO
        );
    }
}
