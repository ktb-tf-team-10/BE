package com.ktb_tf_team_10_be.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TempTokenService {

    public String issue() {
        return "tmp_" + UUID.randomUUID();
    }
}