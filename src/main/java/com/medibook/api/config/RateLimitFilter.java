package com.medibook.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS_PER_MINUTE = 300;
    private final ConcurrentHashMap<String, IpRateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        IpRateLimiter limiter = limiters.computeIfAbsent(clientIp, k -> new IpRateLimiter());

        if (!limiter.tryAcquire()) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Has excedido el límite de solicitudes permitidas. Intenta nuevamente en un minuto.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class IpRateLimiter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long lastMinute = 0;

        public boolean tryAcquire() {
            long currentMinute = System.currentTimeMillis() / 60000;
            
            if (lastMinute != currentMinute) {
                synchronized (this) {
                    if (lastMinute != currentMinute) {
                        lastMinute = currentMinute;
                        count.set(0);
                    }
                }
            }
            
            return count.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
        }
    }
}
