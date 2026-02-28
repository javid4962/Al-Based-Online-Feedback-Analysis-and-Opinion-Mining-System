package com.datavalley.feedback.dto;

import java.time.Instant;

public record ModelVersionResponse(
    Long id,
    String versionName,
    boolean active,
    Instant createdAt
) {
}
