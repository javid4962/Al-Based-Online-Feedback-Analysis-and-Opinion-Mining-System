CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS feedback_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_type VARCHAR(20) NOT NULL,
    source_ref VARCHAR(255) NULL,
    text_raw LONGTEXT NOT NULL,
    rating INT NULL,
    locale VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    ingested_at TIMESTAMP NOT NULL,
    INDEX idx_feedback_submitted_at (submitted_at),
    INDEX idx_feedback_source_type (source_type)
);

CREATE TABLE IF NOT EXISTS feedback_label (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL,
    label_source VARCHAR(20) NOT NULL,
    sentiment_label VARCHAR(20) NOT NULL,
    labeled_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_feedback_label_feedback FOREIGN KEY (feedback_id) REFERENCES feedback_item(id)
);

CREATE TABLE IF NOT EXISTS model_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_name VARCHAR(120) NOT NULL UNIQUE,
    artifact_path VARCHAR(500) NOT NULL,
    vocab_path VARCHAR(500) NULL,
    config_json LONGTEXT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_model_active (is_active)
);

CREATE TABLE IF NOT EXISTS sentiment_prediction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL,
    model_version_id BIGINT NULL,
    sentiment_label VARCHAR(20) NOT NULL,
    confidence DOUBLE NOT NULL,
    logits_json LONGTEXT NULL,
    inferred_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_prediction_feedback FOREIGN KEY (feedback_id) REFERENCES feedback_item(id),
    CONSTRAINT fk_prediction_model FOREIGN KEY (model_version_id) REFERENCES model_version(id),
    INDEX idx_prediction_feedback_inferred (feedback_id, inferred_at)
);

CREATE TABLE IF NOT EXISTS training_run (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL,
    train_size INT NULL,
    test_size INT NULL,
    class_balance_json LONGTEXT NULL,
    metrics_json LONGTEXT NULL,
    notes LONGTEXT NULL
);

CREATE TABLE IF NOT EXISTS job_run (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payload_json LONGTEXT NULL,
    progress INT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NULL,
    INDEX idx_job_status_started (status, started_at)
);

CREATE TABLE IF NOT EXISTS analytics_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_key DATE NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    total_count INT NOT NULL,
    pos_count INT NOT NULL,
    neg_count INT NOT NULL,
    avg_confidence DOUBLE NULL,
    UNIQUE KEY uq_analytics_day_source (date_key, source_type),
    INDEX idx_analytics_day_source (date_key, source_type)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(120) NOT NULL,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(120) NULL,
    target_id VARCHAR(120) NULL,
    payload_json LONGTEXT NULL,
    created_at TIMESTAMP NOT NULL
);
