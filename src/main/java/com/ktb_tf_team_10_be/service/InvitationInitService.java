package com.ktb_tf_team_10_be.service;

import com.ktb_tf_team_10_be.domain.NextStep;
import com.ktb_tf_team_10_be.dto.InitResponse;
import com.ktb_tf_team_10_be.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ktb_tf_team_10_be.domain.invitation.InvitationStepResolver.*;

@Service
@RequiredArgsConstructor
public class InvitationInitService {

    private final InvitationRepository invitationRepository;

    public InitResponse init(String tempToken) {

        return invitationRepository
                .findTopByTempTokenOrderByCreatedAtDesc(tempToken)
                .map(invitation -> {

                    NextStep nextStep =
                            resolveNextStep(
                                    invitation.getStatus()
                            );

                    return new InitResponse(
                            true,
                            invitation.getId().toString(),
                            invitation.getStatus(),
                            nextStep
                    );
                })
                .orElse(
                        InitResponse.noInvitation()
                );
    }
}

