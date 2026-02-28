package com.datavalley.feedback.entity;

import com.datavalley.feedback.enums.LabelSource;
import com.datavalley.feedback.enums.SentimentLabel;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "feedback_label")
public class FeedbackLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private FeedbackItem feedbackItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_source", nullable = false, length = 20)
    private LabelSource labelSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_label", nullable = false, length = 20)
    private SentimentLabel sentimentLabel;

    @Column(name = "labeled_at", nullable = false)
    private Instant labeledAt;

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

    public LabelSource getLabelSource() {
        return labelSource;
    }

    public void setLabelSource(LabelSource labelSource) {
        this.labelSource = labelSource;
    }

    public SentimentLabel getSentimentLabel() {
        return sentimentLabel;
    }

    public void setSentimentLabel(SentimentLabel sentimentLabel) {
        this.sentimentLabel = sentimentLabel;
    }

    public Instant getLabeledAt() {
        return labeledAt;
    }

    public void setLabeledAt(Instant labeledAt) {
        this.labeledAt = labeledAt;
    }
}
