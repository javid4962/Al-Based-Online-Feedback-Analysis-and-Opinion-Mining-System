package com.datavalley.feedback.repository;

import com.datavalley.feedback.entity.JobRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRunRepository extends JpaRepository<JobRun, Long> {
}
