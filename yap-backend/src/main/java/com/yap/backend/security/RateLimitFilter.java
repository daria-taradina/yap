package com.yap.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
public class RateLimitFilter extends OncePerRequestFilter {

    private record BucketEntry(Bucket bucket, long lastAccessed) {}

    private record RateLimitRule(String method, String pathPattern, int tokens, Duration period, boolean perUser) {}

    private final ConcurrentHashMap<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    private final List<RateLimitRule> rules = List.of(
        new RateLimitRule(null, "/api/auth/login", 5, Duration.ofMinutes(1), false),
        new RateLimitRule(null, "/api/auth/register", 5, Duration.ofMinutes(1), false),
        new RateLimitRule("POST", "/api/posts", 5, Duration.ofMinutes(1), true),
        new RateLimitRule("POST", "/api/posts/*/like", 30, Duration.ofMinutes(1), true),
        new RateLimitRule("DELETE", "/api/posts/*/like", 30, Duration.ofMinutes(1), true),
        new RateLimitRule("POST", "/api/posts/*/comments", 10, Duration.ofMinutes(1), true),
        new RateLimitRule("POST", "/api/reports", 3, Duration.ofMinutes(1), true),
        new RateLimitRule("POST", "/api/users/*/follow", 10, Duration.ofMinutes(1), true),
        new RateLimitRule("DELETE", "/api/users/*/unfollow", 10, Duration.ofMinutes(1), true)
    );

    // Fallback rule for unauthenticated GET requests
    private static final int UNAUTHENTICATED_GET_TOKENS = 120;
    private static final Duration UNAUTHENTICATED_GET_PERIOD = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        RateLimitRule matchedRule = findMatchingRule(method, path);

        if (matchedRule != null) {
            String key = buildKey(request, matchedRule);
            if (key == null) {
                // User-based limit but no authenticated user — skip rate limiting
                filterChain.doFilter(request, response);
                return;
            }
            Bucket bucket = resolveBucket(key, matchedRule.tokens(), matchedRule.period());
            if (!bucket.tryConsume(1)) {
                writeRateLimitResponse(response, matchedRule.period());
                return;
            }
        } else if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/")) {
            // Unauthenticated GET fallback
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                String ip = resolveIp(request);
                String key = "GET:/api/**:" + ip;
                Bucket bucket = resolveBucket(key, UNAUTHENTICATED_GET_TOKENS, UNAUTHENTICATED_GET_PERIOD);
                if (!bucket.tryConsume(1)) {
                    writeRateLimitResponse(response, UNAUTHENTICATED_GET_PERIOD);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitRule findMatchingRule(String method, String path) {
        for (RateLimitRule rule : rules) {
            if (rule.method() != null && !rule.method().equalsIgnoreCase(method)) {
                continue;
            }
            if (pathMatches(rule.pathPattern(), path)) {
                return rule;
            }
        }
        return null;
    }

    private boolean pathMatches(String pattern, String path) {
        // Convert pattern with * wildcards to a simple matching logic
        // e.g. "/api/posts/*/like" matches "/api/posts/123/like"
        String[] patternParts = pattern.split("/");
        String[] pathParts = path.split("/");

        if (patternParts.length != pathParts.length) {
            return false;
        }

        for (int i = 0; i < patternParts.length; i++) {
            if ("*".equals(patternParts[i])) {
                continue; // wildcard matches any segment
            }
            if (!patternParts[i].equals(pathParts[i])) {
                return false;
            }
        }
        return true;
    }

    private String buildKey(HttpServletRequest request, RateLimitRule rule) {
        String identifier;
        if (rule.perUser()) {
            String username = resolveUsername();
            if (username == null) {
                return null;
            }
            identifier = "user:" + username;
        } else {
            identifier = "ip:" + resolveIp(request);
        }
        String ruleKey = (rule.method() != null ? rule.method() : "ANY") + ":" + rule.pathPattern();
        return ruleKey + ":" + identifier;
    }

    private Bucket resolveBucket(String key, int tokens, Duration period) {
        long now = System.currentTimeMillis();
        BucketEntry entry = buckets.compute(key, (k, existing) -> {
            if (existing == null) {
                Bucket bucket = Bucket.builder()
                    .addLimit(Bandwidth.builder().capacity(tokens).refillGreedy(tokens, period).build())
                    .build();
                return new BucketEntry(bucket, now);
            }
            return new BucketEntry(existing.bucket(), now);
        });
        return entry.bucket();
    }

    private String resolveIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }

    private void writeRateLimitResponse(HttpServletResponse response, Duration retryAfter) throws IOException {
        long retrySeconds = retryAfter.toSeconds();
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retrySeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"error\":\"Rate limit exceeded\",\"retryAfterSeconds\":" + retrySeconds + "}"
        );
    }

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void evictStaleBuckets() {
        long now = System.currentTimeMillis();
        long threshold = 10 * 60 * 1000L; // 10 minutes
        buckets.entrySet().removeIf(entry ->
            (now - entry.getValue().lastAccessed()) > threshold
        );
    }
}
