package com.ktb_tf_team_10_be.dto;

import com.ktb_tf_team_10_be.domain.Frame;

public record Design2DGenerateReq(

        Groom groom,
        Bride bride,
        Wedding wedding,

        String extraMessage,
        String additionalRequest,
        String tone,
        Frame frame

) {

    public record Groom(
            String name,
            String fatherName,
            String motherName
    ) {}

    public record Bride(
            String name,
            String fatherName,
            String motherName
    ) {}

    public record Wedding(
            String hallName,
            String address,
            String date,   // yyyy-MM-dd
            String time    // HH:mm
    ) {}
}
