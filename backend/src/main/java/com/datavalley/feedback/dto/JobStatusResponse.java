package com.datavalley.feedback.dto;

import java.time.Instant;

public record JobStatusResponse(
    Long jobId,
    String jobType,
    String status,
    Integer progress,
    String payload,
    Instant startedAt,
    Instant endedAt
) {
}
