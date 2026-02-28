package com.datavalley.feedback.repository;

import com.datavalley.feedback.entity.FeedbackLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackLabelRepository extends JpaRepository<FeedbackLabel, Long> {
}
