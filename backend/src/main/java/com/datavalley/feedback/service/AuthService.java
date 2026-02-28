package com.datavalley.feedback.service;

import com.datavalley.feedback.dto.AuthTokenResponse;
import com.datavalley.feedback.dto.LoginRequest;

public interface AuthService {
    AuthTokenResponse login(LoginRequest request);
}
