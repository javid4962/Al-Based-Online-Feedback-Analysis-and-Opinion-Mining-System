package com.datavalley.feedback.service;

public interface AuditLogService {
    void log(String actor, String action, String targetType, String targetId, String payloadJson);
}
