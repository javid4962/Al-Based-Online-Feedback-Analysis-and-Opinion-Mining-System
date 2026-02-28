package com.datavalley.feedback.dto;

import com.datavalley.feedback.enums.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record FeedbackCreateRequest(
    @NotNull SourceType sourceType,
    @Size(max = 255) String sourceRef,
    @NotBlank String text,
    Integer rating,
    @NotBlank @Size(max = 20) String locale,
    Instant submittedAt
) {
}
