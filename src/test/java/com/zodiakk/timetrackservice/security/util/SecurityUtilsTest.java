package com.zodiakk.timetrackservice.security.util;

import com.zodiakk.timetrackservice.common.constants.SecurityConstants;
import com.zodiakk.timetrackservice.security.principal.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUtilsTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsCurrentUserAndRoles() {
        UUID userId = UUID.randomUUID();
        CurrentUser principal = CurrentUser.builder()
                .userId(userId)
                .role(SecurityConstants.ADMIN_AUTHORITY)
                .email("admin@company.com")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority(SecurityConstants.ADMIN_AUTHORITY))
                )
        );

        assertEquals(userId, SecurityUtils.getCurrentUserId());
        assertTrue(SecurityUtils.isAdmin());
        assertTrue(SecurityUtils.hasAnyRole(SecurityConstants.HR_AUTHORITY, SecurityConstants.ADMIN_AUTHORITY));
        assertFalse(SecurityUtils.hasRole(SecurityConstants.EMPLOYEE_AUTHORITY));
        assertTrue(SecurityUtils.keysMatch("secret", "secret"));
        assertFalse(SecurityUtils.keysMatch("secret", "other"));
        assertFalse(SecurityUtils.keysMatch(" ", "secret"));
    }

    @Test
    void missingAuthenticationFails() {
        assertThrows(IllegalStateException.class, SecurityUtils::getCurrentUser);
    }
}
