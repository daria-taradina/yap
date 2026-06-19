package com.yap.backend.config;

import net.jqwik.api.*;
import net.jqwik.spring.JqwikSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.yap.backend.security.JwtAuthFilter;
import com.yap.backend.security.JwtUtil;
import com.yap.backend.security.UserDetailsServiceImpl;
import com.yap.backend.services.*;

import java.util.List;

/**
 * Property-based tests for SecurityConfig.
 *
 * Feature: yap-v1-launch, Property 1: Public GET access permits unauthenticated requests
 * Feature: yap-v1-launch, Property 2: Non-GET requests require authentication
 *
 * Validates: Requirements 1.1, 1.2, 1.4
 */
@JqwikSpringSupport
@WebMvcTest
@Import({SecurityConfig.class, JwtAuthFilter.class})
class SecurityConfigPropertyTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock all dependencies needed by the security filter chain and controllers
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

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

    // --- Public GET endpoint path segments ---
    private static final List<String> PUBLIC_GET_PATHS = List.of(
        "/api/posts",
        "/api/posts/1",
        "/api/posts/tag/java",
        "/api/spaces",
        "/api/spaces/1",
        "/api/users/testuser",
        "/api/users/testuser/posts",
        "/api/notifications"
    );

    // --- Mutation methods (non-GET) ---
    private static final List<String> MUTATION_METHODS = List.of(
        "POST", "PUT", "PATCH", "DELETE"
    );

    // --- Non-auth endpoint paths for mutation testing ---
    private static final List<String> NON_AUTH_PATHS = List.of(
        "/api/posts",
        "/api/posts/1",
        "/api/posts/1/like",
        "/api/posts/1/comments",
        "/api/spaces",
        "/api/spaces/1",
        "/api/spaces/1/join",
        "/api/users/me",
        "/api/users/1/follow",
        "/api/notifications",
        "/api/notifications/read-all"
    );

    /**
     * Property 1: Public GET access permits unauthenticated requests.
     *
     * For any valid public GET endpoint path (feed, posts, spaces, profiles),
     * an unauthenticated request SHALL receive a successful response (not 401),
     * while the same path with a non-GET method SHALL receive 401.
     *
     * Validates: Requirements 1.1, 1.2
     */
    @Property(tries = 100)
    void publicGetEndpointsPermitUnauthenticatedAccess(
            @ForAll("publicGetPaths") String path,
            @ForAll("mutationMethods") String mutationMethod) throws Exception {

        // GET request to public endpoint should NOT return 401
        int getStatus = mockMvc.perform(MockMvcRequestBuilders.get(path))
                .andReturn()
                .getResponse()
                .getStatus();

        // Should not be 401 - the request is permitted (may be 200 or 404 depending on data)
        assert getStatus != 401 :
                "GET " + path + " returned 401 but should be publicly accessible. Got: " + getStatus;

        // Same path with mutation method should return 401 for unauthenticated request
        var requestBuilder = switch (mutationMethod) {
            case "POST" -> MockMvcRequestBuilders.post(path);
            case "PUT" -> MockMvcRequestBuilders.put(path);
            case "PATCH" -> MockMvcRequestBuilders.patch(path);
            case "DELETE" -> MockMvcRequestBuilders.delete(path);
            default -> throw new IllegalArgumentException("Unknown method: " + mutationMethod);
        };

        int mutationStatus = mockMvc.perform(requestBuilder)
                .andReturn()
                .getResponse()
                .getStatus();

        assert mutationStatus == 401 :
                mutationMethod + " " + path + " should return 401 but got: " + mutationStatus;
    }

    /**
     * Property 2: Non-GET requests require authentication.
     *
     * For any HTTP method in {POST, PUT, PATCH, DELETE} and any endpoint path
     * not matching `/api/auth/**`, an unauthenticated request SHALL receive HTTP 401.
     *
     * Validates: Requirements 1.2, 1.4
     */
    @Property(tries = 100)
    void nonGetRequestsRequireAuthentication(
            @ForAll("mutationMethods") String method,
            @ForAll("nonAuthPaths") String path) throws Exception {

        var requestBuilder = switch (method) {
            case "POST" -> MockMvcRequestBuilders.post(path);
            case "PUT" -> MockMvcRequestBuilders.put(path);
            case "PATCH" -> MockMvcRequestBuilders.patch(path);
            case "DELETE" -> MockMvcRequestBuilders.delete(path);
            default -> throw new IllegalArgumentException("Unknown method: " + method);
        };

        int status = mockMvc.perform(requestBuilder)
                .andReturn()
                .getResponse()
                .getStatus();

        assert status == 401 :
                method + " " + path + " should return 401 for unauthenticated request but got: " + status;
    }

    // --- Arbitrary providers ---

    @Provide
    Arbitrary<String> publicGetPaths() {
        return Arbitraries.of(PUBLIC_GET_PATHS);
    }

    @Provide
    Arbitrary<String> mutationMethods() {
        return Arbitraries.of(MUTATION_METHODS);
    }

    @Provide
    Arbitrary<String> nonAuthPaths() {
        // Generate paths that don't match /api/auth/**
        Arbitrary<String> staticPaths = Arbitraries.of(NON_AUTH_PATHS);

        // Also generate random sub-paths under /api/ (not /api/auth/)
        List<String> prefixes = List.of("/api/posts/", "/api/spaces/", "/api/users/");
        Arbitrary<String> dynamicPaths = Arbitraries.of(prefixes)
                .flatMap(prefix -> Arbitraries.integers().between(1, 9999)
                        .map(id -> prefix + id));

        return Arbitraries.oneOf(staticPaths, dynamicPaths);
    }
}
