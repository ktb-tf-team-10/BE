package com.ktb_tf_team_10_be.domain;

import com.ktb_tf_team_10_be.domain.NextStep;
import com.ktb_tf_team_10_be.dto.InvitationStatus;

public class InvitationStepResolver {

    public static NextStep resolveNextStep(InvitationStatus status) {

        return switch (status) {

            case INIT -> null;
            case CREATED,
                 BASIC_INFO_DONE ->
                    NextStep.SELECT_TONE;

            case TONE_SELECTED ->
                    NextStep.EDIT_MESSAGE;

            case MESSAGE_DONE ->
                    NextStep.UPLOAD_IMAGES;

            case IMAGE_UPLOADED,
                 DESIGN_PROCESSING ->
                    NextStep.DESIGN_INVITATION;

            case DESIGN_DONE ->
                    NextStep.VIEW_DESIGN_RESULT;

            case FEATURE_SELECTED,
                 MODEL_3D_PROCESSING ->
                    NextStep.UPLOAD_3D_PHOTOS;

            case MODEL_3D_DONE ->
                    NextStep.CREATE_POSTER;

            case POSTER_PROCESSING,
                 POSTER_DONE,
                 COMPLETED ->
                    NextStep.VIEW_FINAL_RESULT;

            case FAILED ->
                    NextStep.START_BASIC_INFO;
        };
    }
}
