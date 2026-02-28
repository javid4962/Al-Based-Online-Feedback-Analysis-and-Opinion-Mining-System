package com.datavalley.feedback.entity;

import com.datavalley.feedback.enums.SentimentLabel;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "sentiment_prediction")
public class SentimentPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private FeedbackItem feedbackItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_version_id")
    private ModelVersion modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_label", nullable = false, length = 20)
    private SentimentLabel sentimentLabel;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "logits_json", columnDefinition = "LONGTEXT")
    private String logitsJson;

    @Column(name = "inferred_at", nullable = false)
    private Instant inferredAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FeedbackItem getFeedbackItem() {
        return feedbackItem;
    }

    public void setFeedbackItem(FeedbackItem feedbackItem) {
        this.feedbackItem = feedbackItem;
    }

    public ModelVersion getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(ModelVersion modelVersion) {
        this.modelVersion = modelVersion;
    }

    public SentimentLabel getSentimentLabel() {
        return sentimentLabel;
    }

    public void setSentimentLabel(SentimentLabel sentimentLabel) {
        this.sentimentLabel = sentimentLabel;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getLogitsJson() {
        return logitsJson;
    }

    public void setLogitsJson(String logitsJson) {
        this.logitsJson = logitsJson;
    }

    public Instant getInferredAt() {
        return inferredAt;
    }

    public void setInferredAt(Instant inferredAt) {
        this.inferredAt = inferredAt;
    }
}
