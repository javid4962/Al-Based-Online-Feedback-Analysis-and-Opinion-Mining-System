package com.datavalley.feedback.dto;

import java.time.LocalDate;

public record ReanalyzeRequest(
    LocalDate fromDate,
    LocalDate toDate,
    String sourceType
) {
}
