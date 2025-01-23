package com.backend.identityservice.service;

import com.backend.identityservice.dto.request.*;
import com.backend.identityservice.dto.response.AuthenticationResponse;
import com.backend.identityservice.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {

    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;

    AuthenticationResponse outboundAuthenticate(String code);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void logout() throws ParseException, JOSEException;

    AuthenticationResponse refreshAccessToken() throws ParseException, JOSEException;

    void resetPassword(ResetPasswordRequest request) throws ParseException, JOSEException;

    void changePassword(ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void activeAccount(ActivationTokenRequest request);

}
