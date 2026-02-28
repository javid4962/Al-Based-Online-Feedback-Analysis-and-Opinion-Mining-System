package com.datavalley.feedback.repository;

import com.datavalley.feedback.entity.TrainingRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRunRepository extends JpaRepository<TrainingRun, Long> {
}
