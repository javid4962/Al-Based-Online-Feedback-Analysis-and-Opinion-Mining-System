package com.datavalley.feedback.entity;

import com.datavalley.feedback.enums.TrainingRunStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "training_run")
public class TrainingRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TrainingRunStatus status;

    @Column(name = "train_size")
    private Integer trainSize;

    @Column(name = "test_size")
    private Integer testSize;

    @Column(name = "class_balance_json", columnDefinition = "LONGTEXT")
    private String classBalanceJson;

    @Column(name = "metrics_json", columnDefinition = "LONGTEXT")
    private String metricsJson;

    @Column(name = "notes", columnDefinition = "LONGTEXT")
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public TrainingRunStatus getStatus() {
        return status;
    }

    public void setStatus(TrainingRunStatus status) {
        this.status = status;
    }

    public Integer getTrainSize() {
        return trainSize;
    }

    public void setTrainSize(Integer trainSize) {
        this.trainSize = trainSize;
    }

    public Integer getTestSize() {
        return testSize;
    }

    public void setTestSize(Integer testSize) {
        this.testSize = testSize;
    }

    public String getClassBalanceJson() {
        return classBalanceJson;
    }

    public void setClassBalanceJson(String classBalanceJson) {
        this.classBalanceJson = classBalanceJson;
    }

    public String getMetricsJson() {
        return metricsJson;
    }

    public void setMetricsJson(String metricsJson) {
        this.metricsJson = metricsJson;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
