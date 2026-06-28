package com.yap.backend.security;

import com.yap.backend.config.SecurityConfig;
import com.yap.backend.entities.User;
import com.yap.backend.repositories.UserRepository;
import com.yap.backend.services.*;
import net.jqwik.api.*;
import net.jqwik.spring.JqwikSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.when;

/**
 * Property-based tests for RateLimitFilter and security hardening.
 *
 * Property 10: Rate limiter blocks request exceeding configured limit per path
 * Property 11: Rate limiter resolves IP from X-Forwarded-For
 * Property 12: Rate limiter applies user-based limits using JWT identity
 * Property 13: Banned user receives 403 on all endpoints
 *
 * Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.6
 */
@JqwikSpringSupport
@WebMvcTest
@Import({SecurityConfig.class, JwtAuthFilter.class, RateLimitFilter.class})
class RateLimitPropertyTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock all dependencies needed by the security filter chain
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PostManagementService postManagementService;

    @MockitoBean
    private PostInteractionService postInteractionService;

    @MockitoBean
    private CommunityService communityService;

    @MockitoBean
    private CommunityMemberService communityMemberService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserFollowService userFollowService;

    // Counter to ensure unique IPs/usernames across property runs (avoids bucket collision)
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    /**
     * Property 10: Rate limiter blocks request exceeding configured limit per path.
     *
     * For any IP address sending requests to /api/auth/login or /api/auth/register,
     * the 6th request within a 1-minute window SHALL receive HTTP 429 with a valid
     * Retry-After header.
     *
     * Validates: Requirements 11.1, 11.2, 11.3
     */
    @Property(tries = 10)
    void rateLimiterBlocksRequestExceedingConfiguredLimitPerPath(
            @ForAll("authPaths") String path) throws Exception {

        // Use a unique IP per property invocation to avoid bucket collisions
        int uniqueId = COUNTER.incrementAndGet();
        String ipAddress = "10." + (uniqueId / 65536 % 256) + "." + (uniqueId / 256 % 256) + "." + (uniqueId % 256);

        // Send 5 requests — all should succeed (not 429)
        for (int i = 0; i < 5; i++) {
            int status = mockMvc.perform(MockMvcRequestBuilders.post(path)
                            .with(request -> {
                                request.setRemoteAddr(ipAddress);
                                return request;
                            }))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            assert status != 429 :
                    "Request " + (i + 1) + " to " + path + " from IP " + ipAddress
                            + " should not be rate limited but got 429";
        }

        // The 6th request should be rate limited
        var response = mockMvc.perform(MockMvcRequestBuilders.post(path)
                        .with(request -> {
                            request.setRemoteAddr(ipAddress);
                            return request;
                        }))
                .andReturn()
                .getResponse();

        assert response.getStatus() == 429 :
                "6th request to " + path + " from IP " + ipAddress
                        + " should return 429 but got: " + response.getStatus();

        String retryAfter = response.getHeader("Retry-After");
        assert retryAfter != null :
                "Response should include Retry-After header";

        int retrySeconds = Integer.parseInt(retryAfter);
        assert retrySeconds > 0 :
                "Retry-After should be a positive number, got: " + retrySeconds;
    }

    /**
     * Property 11: Rate limiter resolves IP from X-Forwarded-For.
     *
     * For any request with an X-Forwarded-For header, the rate limiter SHALL use the
     * first IP value in that header for bucket resolution, and different first-IP values
     * SHALL be tracked independently.
     *
     * Validates: Requirements 11.4
     */
    @Property(tries = 10)
    void rateLimiterResolvesIpFromXForwardedFor() throws Exception {

        String path = "/api/auth/login";

        // Use unique IPs for this property run
        int uniqueId = COUNTER.incrementAndGet();
        String ip1 = "20." + (uniqueId / 65536 % 256) + "." + (uniqueId / 256 % 256) + "." + (uniqueId % 256);
        int uniqueId2 = COUNTER.incrementAndGet();
        String ip2 = "30." + (uniqueId2 / 65536 % 256) + "." + (uniqueId2 / 256 % 256) + "." + (uniqueId2 % 256);

        // Exhaust rate limit for ip1 using X-Forwarded-For
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post(path)
                    .header("X-Forwarded-For", ip1 + ", 10.0.0.1"));
        }

        // 6th request from ip1 should be blocked
        int status1 = mockMvc.perform(MockMvcRequestBuilders.post(path)
                        .header("X-Forwarded-For", ip1 + ", 10.0.0.1"))
                .andReturn()
                .getResponse()
                .getStatus();

        assert status1 == 429 :
                "6th request from X-Forwarded-For IP " + ip1
                        + " should be blocked but got: " + status1;

        // Request from ip2 should NOT be blocked (independent bucket)
        int status2 = mockMvc.perform(MockMvcRequestBuilders.post(path)
                        .header("X-Forwarded-For", ip2 + ", 10.0.0.1"))
                .andReturn()
                .getResponse()
                .getStatus();

        assert status2 != 429 :
                "Request from different X-Forwarded-For IP " + ip2
                        + " should not be rate limited but got 429";
    }

    /**
     * Property 12: Rate limiter applies user-based limits using JWT identity.
     *
     * For user-based rate limit rules (e.g., POST /api/posts: 5/min/user), the rate
     * limiter uses the authenticated user's identity for tracking. Different users
     * have independent limits.
     *
     * Validates: Requirements 11.1 (expanded)
     */
    @Property(tries = 10)
    void rateLimiterAppliesUserBasedLimitsUsingJwtIdentity() throws Exception {

        String path = "/api/posts";

        // Use unique usernames per property run
        int uniqueId = COUNTER.incrementAndGet();
        String user1 = "ratelimituser" + uniqueId + "a";
        String user2 = "ratelimituser" + uniqueId + "b";
        String token1 = "token-rl-" + uniqueId + "-a";
        String token2 = "token-rl-" + uniqueId + "-b";

        // Set up mocks for both users
        setupAuthenticatedUser(user1, token1, false);
        setupAuthenticatedUser(user2, token2, false);

        // Exhaust rate limit for user1 (5 POST /api/posts)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post(path)
                    .header("Authorization", "Bearer " + token1)
                    .contentType("application/json")
                    .content("{}"));
        }

        // 6th request from user1 should be rate limited
        int status1 = mockMvc.perform(MockMvcRequestBuilders.post(path)
                        .header("Authorization", "Bearer " + token1)
                        .contentType("application/json")
                        .content("{}"))
                .andReturn()
                .getResponse()
                .getStatus();

        assert status1 == 429 :
                "6th POST /api/posts from user " + user1
                        + " should return 429 but got: " + status1;

        // Clear security context so user2's token is processed fresh by JwtAuthFilter
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        // user2 should NOT be rate limited (independent bucket)
        int status2 = mockMvc.perform(MockMvcRequestBuilders.post(path)
                        .header("Authorization", "Bearer " + token2)
                        .contentType("application/json")
                        .content("{}"))
                .andReturn()
                .getResponse()
                .getStatus();

        assert status2 != 429 :
                "POST /api/posts from different user " + user2
                        + " should not be rate limited but got 429";
    }

    /**
     * Property 13: Banned user receives 403 on all endpoints.
     *
     * For any user where isBanned is true, ALL requests (even with valid JWT)
     * SHALL receive HTTP 403.
     *
     * Validates: (security hardening)
     */
    @Property(tries = 20)
    void bannedUserReceives403OnAllEndpoints(
            @ForAll("protectedEndpoints") String path) throws Exception {

        int uniqueId = COUNTER.incrementAndGet();
        String username = "banneduser" + uniqueId;
        String token = "token-banned-" + uniqueId;

        // Set up mock for a banned user
        setupAuthenticatedUser(username, token, true);

        int status = mockMvc.perform(MockMvcRequestBuilders.get(path)
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getStatus();

        assert status == 403 :
                "Banned user " + username + " requesting " + path
                        + " should receive 403 but got: " + status;
    }

    // --- Helper methods ---

    private void setupAuthenticatedUser(String username, String token, boolean isBanned) {
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn(username);

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("encoded-password");
        user.setBanned(isBanned);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        username, "encoded-password",
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    }

    // --- Arbitrary providers ---

    @Provide
    Arbitrary<String> authPaths() {
        return Arbitraries.of("/api/auth/login", "/api/auth/register");
    }

    @Provide
    Arbitrary<String> protectedEndpoints() {
        return Arbitraries.of(
                "/api/posts",
                "/api/posts/1",
                "/api/users/me",
                "/api/spaces/my",
                "/api/notifications",
                "/api/bookmarks"
        );
    }
}
