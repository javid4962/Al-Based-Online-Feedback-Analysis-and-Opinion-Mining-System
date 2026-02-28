package com.datavalley.feedback.service.impl;

import com.datavalley.feedback.dto.AuthTokenResponse;
import com.datavalley.feedback.dto.LoginRequest;
import com.datavalley.feedback.entity.AdminUser;
import com.datavalley.feedback.repository.AdminUserRepository;
import com.datavalley.feedback.security.JwtService;
import com.datavalley.feedback.service.AuthService;
import com.datavalley.feedback.service.AuditLogService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AdminUserRepository adminUserRepository;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           AdminUserRepository adminUserRepository,
                           AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.adminUserRepository = adminUserRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public AuthTokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtService.generateToken(request.username());
        adminUserRepository.findByUsername(request.username()).ifPresent(admin -> {
            admin.setLastLoginAt(Instant.now());
            adminUserRepository.save(admin);
        });

        auditLogService.log(request.username(), "LOGIN", "ADMIN_USER", request.username(), "{}");
        return new AuthTokenResponse(token, jwtService.getExpirationInstant());
    }
}
