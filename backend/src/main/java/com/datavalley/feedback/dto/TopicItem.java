package com.datavalley.feedback.dto;

public record TopicItem(
    String token,
    long count,
    String sentiment
) {
}
