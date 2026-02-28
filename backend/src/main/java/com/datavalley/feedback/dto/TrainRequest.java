package com.datavalley.feedback.dto;

public record TrainRequest(
    String datasetPolicy,
    Integer epochs,
    Integer batchSize,
    Double learningRate,
    Boolean rebalance
) {
}
