// PURPOSE: Auth/login exception mapping must not turn CORS or empty-body failures into 500.
package com.biopower.exception;

import com.biopower.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void badCredentialsAreUnauthorized() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentials(new BadCredentialsException("bad"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void disabledAccountsAreUnauthorizedNotForbidden() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDisabledAccount(new DisabledException("off"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Account is disabled", response.getBody().getMessage());
    }

    @Test
    void missingLoginBodyIsBadRequestNotServerError() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleUnreadable(new HttpMessageNotReadableException("empty"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request body is required", response.getBody().getMessage());
    }
}
