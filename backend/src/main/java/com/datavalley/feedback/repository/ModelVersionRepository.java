package com.datavalley.feedback.repository;

import com.datavalley.feedback.entity.ModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModelVersionRepository extends JpaRepository<ModelVersion, Long> {
    Optional<ModelVersion> findByActiveTrue();
}
