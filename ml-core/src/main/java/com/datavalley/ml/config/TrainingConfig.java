package com.datavalley.ml.config;

public record TrainingConfig(
    String datasetPolicy,
    boolean rebalance,
    HbgruLstmConfig model
) {
}
