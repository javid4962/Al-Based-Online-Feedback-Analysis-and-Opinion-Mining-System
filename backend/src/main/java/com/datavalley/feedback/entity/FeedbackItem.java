package com.datavalley.feedback.entity;

import com.datavalley.feedback.enums.SourceType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "feedback_item")
public class FeedbackItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @Column(name = "source_ref", length = 255)
    private String sourceRef;

    @Column(name = "text_raw", nullable = false, columnDefinition = "LONGTEXT")
    private String textRaw;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "locale", nullable = false, length = 20)
    private String locale;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public String getTextRaw() {
        return textRaw;
    }

    public void setTextRaw(String textRaw) {
        this.textRaw = textRaw;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(Instant ingestedAt) {
        this.ingestedAt = ingestedAt;
    }
}
