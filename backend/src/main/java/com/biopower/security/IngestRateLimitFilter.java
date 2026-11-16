// PURPOSE: Authentication/authorization component for SWARM API security.
package com.biopower.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple fixed-window rate limit for ingest and login. Process-local (sufficient for single-node Pi).
 */
@Component
@RequiredArgsConstructor
public class IngestRateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Value("${biopower.rate-limit.ingest-per-minute:120}")
    private int ingestPerMinute;

    @Value("${biopower.rate-limit.login-per-minute:20}")
    private int loginPerMinute;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null) {
            return true;
        }
        if ("POST".equalsIgnoreCase(request.getMethod()) && path.startsWith("/iot/")) {
            return false;
        }
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/auth/login".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        boolean login = "/auth/login".equals(path);
        int limit = login ? loginPerMinute : ingestPerMinute;
        String key = (login ? "login:" : "ingest:") + clientKey(request);
        long minute = System.currentTimeMillis() / 60_000L;
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || existing.minute != minute) {
                return new Window(minute, new AtomicInteger(0));
            }
            return existing;
        });
        if (window.count.incrementAndGet() > limit) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                    "success", false,
                    "message", "Rate limit exceeded; retry later"
            ));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String device = request.getHeader(DeviceAuthenticationFilter.HEADER_DEVICE_ID);
        if (device != null && !device.isBlank()) {
            return device.trim();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private record Window(long minute, AtomicInteger count) {
    }
}
