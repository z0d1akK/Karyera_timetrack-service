package com.zodiakk.timetrackservice.security.handler;

import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SecurityHandlerTest {

    @Test
    void entryPointWritesUnauthorized() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CustomAuthenticationEntryPoint().commence(
                mock(HttpServletRequest.class),
                response,
                new BadCredentialsException("Некорректный внутренний ключ")
        );

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("Некорректный внутренний ключ"));
    }

    @Test
    void entryPointUsesDefaultMessage() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CustomAuthenticationEntryPoint().commence(
                mock(HttpServletRequest.class),
                response,
                new InsufficientAuthenticationException("anonymous")
        );

        assertTrue(response.getContentAsString().contains(ErrorMessages.UNAUTHORIZED));
    }

    @Test
    void accessDeniedWritesForbidden() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CustomAccessDeniedHandler().handle(
                mock(HttpServletRequest.class),
                response,
                new AccessDeniedException("denied")
        );

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertTrue(response.getContentAsString().contains(ErrorMessages.ACCESS_DENIED));
    }
}
