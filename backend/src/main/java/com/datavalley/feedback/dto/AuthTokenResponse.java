package com.datavalley.feedback.dto;

import java.time.Instant;

public record AuthTokenResponse(
    String accessToken,
    Instant expiresAt
) {
}
