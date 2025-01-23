package com.backend.identityservice.controller;

import com.backend.dto.response.ApiResponse;
import com.backend.identityservice.dto.request.*;
import com.backend.identityservice.dto.response.AuthenticationResponse;
import com.backend.identityservice.dto.response.IntrospectResponse;
import com.backend.identityservice.service.impl.AuthenticationServiceImpl;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationServiceImpl authenticationServiceImpl;

    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(@RequestParam("code") String code) {
        var result = authenticationServiceImpl.outboundAuthenticate(code);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        var result = authenticationServiceImpl.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws JOSEException, ParseException {
        var result = authenticationServiceImpl.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshAccessToken() throws ParseException, JOSEException {
        var result = authenticationServiceImpl.refreshAccessToken();
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() throws ParseException, JOSEException {
        authenticationServiceImpl.logout();
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/change-password")
    ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        authenticationServiceImpl.changePassword(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) throws ParseException, JOSEException {
        authenticationServiceImpl.resetPassword(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authenticationServiceImpl.forgotPassword(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/active-account")
    ApiResponse<Void> activeAccount(@RequestBody ActivationTokenRequest request) {
        authenticationServiceImpl.activeAccount(request);
        return ApiResponse.<Void>builder()
                .build();
    }
}
