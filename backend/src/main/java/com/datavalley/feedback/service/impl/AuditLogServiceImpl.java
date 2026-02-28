package com.datavalley.feedback.service.impl;

import com.datavalley.feedback.entity.AuditLog;
import com.datavalley.feedback.repository.AuditLogRepository;
import com.datavalley.feedback.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(String actor, String action, String targetType, String targetId, String payloadJson) {
        AuditLog log = new AuditLog();
        log.setActor(actor == null ? "system" : actor);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setPayloadJson(payloadJson);
        log.setCreatedAt(Instant.now());
        auditLogRepository.save(log);
    }
}
