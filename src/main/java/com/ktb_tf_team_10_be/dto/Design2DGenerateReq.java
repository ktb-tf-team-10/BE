package com.ktb_tf_team_10_be.dto;

public record Design2DGenerateReq(
        Groom groom,
        Bride bride,
        Wedding wedding,
        String extraMessage,
        String additionalRequest,
        String tone
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
