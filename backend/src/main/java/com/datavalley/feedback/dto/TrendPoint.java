package com.datavalley.feedback.dto;

import java.time.LocalDate;

public record TrendPoint(
    LocalDate date,
    long total,
    long positive,
    long negative
) {
}
