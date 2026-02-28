package com.datavalley.ml.config;

public record HbgruLstmConfig(
    int embeddingSize,
    int bidirectionalGruUnits,
    int lstmUnits,
    double dropout,
    double learningRate,
    int batchSize,
    int epochs,
    int earlyStoppingPatience
) {
    public static HbgruLstmConfig defaultConfig() {
        return new HbgruLstmConfig(128, 256, 128, 0.20, 0.001, 128, 100, 8);
    }
}
