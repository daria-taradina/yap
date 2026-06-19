# Design Document

## Overview

This document defines the technical design for Yap's V1 launch, covering all 18 requirements across three phases. The design leverages the existing Java Spring Boot 4.x backend with PostgreSQL and React + Vite frontend, staying within the $0 budget constraint.

**Key design principles:**
- No AI moderation — moderation is purely report-based (users report → founder reviews in mod queue)
- All services use free tiers (Vercel, Render, Resend, Giphy, cron-job.org)
- No Redis, no Elasticsearch, no paid APIs
- Progressive Web App for mobile experience instead of native apps

## Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         VERCEL (Frontend)                            │
│  React + Vite SPA + Service Worker + Web Manifest (PWA)             │
│  - Feed, Spaces, Posts, Comments, Profiles (public read)            │
│  - Mod Queue (role-gated, report-driven)                            │
│  - Search, Bookmarks, Tag Pills, GIF Picker                        │
│  - Bottom nav + responsive layout for mobile                        │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ HTTPS (CORS: Vercel prod + localhost)
┌───────────────────────────▼─────────────────────────────────────────┐
│                      RENDER (Backend)                                │
│  Spring Boot 4.x + PostgreSQL (free tier)                           │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐  │
│  │ SecurityConfig│  │ Rate Limiter │  │ JWT Auth Filter           │  │
│  │ (public GET) │  │ (Bucket4j)   │  │                           │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────────┘  │
│         │                  │                     │                   │
│  ┌──────▼──────────────────▼─────────────────────▼───────────────┐  │
│  │                    Controllers                                 │  │
│  │  Auth, Posts, Communities, Profile, Reports, Search, Bookmarks│  │
│  └──────────────────────────┬────────────────────────────────────┘  │
│                             │                                        │
│  ┌──────────────────────────▼────────────────────────────────────┐  │
│  │                     Services                                   │  │
│  │  Auth (email verify, password reset) │ Feed (algorithm)        │  │
│  │  Report (user-reported content)      │ Search (ILIKE)          │  │
│  │  Bookmark │ Tag                                                │  │
│  └──────────────────────────┬────────────────────────────────────┘  │
│                             │                                        │
│  ┌──────────────────────────▼────────────────────────────────────┐  │
│  │                   PostgreSQL (Render)                           │  │
│  │  users, posts, comments, communities, reports, bookmarks, tags │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘

External Services (all free):
  - Resend (transactional email, free tier: 100 emails/day)
  - Giphy API (GIF search, free tier)
  - cron-job.org (keep-alive pings every 14 min, free)
  - vite-plugin-pwa (build-time PWA generation)
```

## Components and Interfaces

### Backend Components

| Component | Responsibility |
|-----------|---------------|
| SecurityConfig | Public GET access, CORS, auth filter chain |
| RateLimitFilter | Bucket4j-based IP rate limiting on auth endpoints |
| JwtAuthFilter | Token extraction and validation (existing) |
| AuthController | Login, register, verify email, reset password |
| PostManagementController | CRUD for posts (existing) |
| ReportController | Create reports, list reported posts for mod queue |
| BookmarkController | Toggle bookmarks, list saved posts |
| SearchController | Keyword search across posts/spaces/users |
| FeedService | Feed algorithm with time-decay + velocity + discovery |
| TagService | Trending tags, tag-based post retrieval (existing) |

### Frontend Components

| Component | Responsibility |
|-----------|---------------|
| FeedPage | Main feed with tag filtering, algorithm-ranked posts |
| SpacePage | Space view with inline "New Discussion" button |
| ForumCard | Post summary with clickable space/user/tag links |
| PostDetailPage | Full post with delete button, report button |
| ModQueuePage | Report-driven moderation (ADMIN/MOD only) |
| SavedPage | Bookmarked posts list |
| SearchPage | Tabbed search results (Posts/Spaces/Users) |
| TagInput | Pill-based tag input for post creation |
| GifPicker | Giphy-powered GIF search and selection |
| BottomNav | Mobile navigation bar (Home/Search/Create/Profile) |
| OfflineFallback | PWA offline page |

### API Endpoints (New/Modified)

```
# Phase 1
GET  /actuator/health              — Public, keep-alive target
GET  /api/posts, /api/feed, etc.   — Now public (no JWT required)

# Phase 2
POST /api/auth/verify-email        — Verify email token
POST /api/auth/resend-verification — Resend verification email
POST /api/auth/forgot-password     — Request password reset
POST /api/auth/reset-password      — Submit new password with token
GET  /api/mod/reports              — Paginated pending reports (ADMIN/MOD)
PATCH /api/mod/reports/{id}/dismiss — Dismiss report
PATCH /api/mod/reports/{id}/remove  — Remove reported post

# Phase 3
POST /api/reports                  — Submit a report
GET  /api/bookmarks                — List user's bookmarks (paginated)
POST /api/bookmarks/{postId}       — Toggle bookmark
GET  /api/search?q=&type=&page=    — Search posts/spaces/users
GET  /api/feed                     — Algorithm-ranked feed
```

## Data Models

### New Entity: Bookmark

```java
@Entity
@Table(name = "bookmarks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class Bookmark {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### New Fields on User Entity

```java
// Email verification
private boolean emailVerified = false;
private String verificationToken;        // 32+ char random token
private LocalDateTime verificationTokenExpiry;  // createdAt + 24h

// Password reset
private String resetToken;               // 32+ char random token
private LocalDateTime resetTokenExpiry;  // createdAt + 1h

// Session invalidation (for password reset)
private LocalDateTime passwordChangedAt; // reject JWTs issued before this
```

### Modified ReportStatus Enum

```java
public enum ReportStatus {
    PENDING,    // awaiting mod review
    DISMISSED,  // mod dismissed the report (content stays)
    RESOLVED    // mod removed the content
}
```

Note: `REVIEWED` is replaced with `DISMISSED` and `RESOLVED` for clarity in the mod queue workflow.

### Post Entity — Fields to Remove (Future Cleanup)

The existing `toxicityScore` and `isFlagged` fields on the Post entity are vestigial from the removed Perspective API integration. They remain in the schema for now (no migration needed) but are unused in V1 logic. The `isRemoved` field continues to be used by the mod queue for manual removal.

---

## Phase 1 Design: Pre-Launch Blockers

### 1. Public Read Access (Requirement 1)

**Approach:** Modify `SecurityConfig.filterChain()` to permit unauthenticated GET requests while requiring auth for mutations.

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/actuator/health").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
    .requestMatchers(HttpMethod.GET, "/api/spaces/my").authenticated()
    .anyRequest().authenticated()
)
```

**CORS:** Add production Vercel URL via environment variable:
```java
config.setAllowedOrigins(List.of(
    "http://localhost:5173",
    Optional.ofNullable(System.getenv("FRONTEND_URL")).orElse("http://localhost:5173")
));
```

**401 Response:** Configure a custom `AuthenticationEntryPoint` that returns JSON `{"error": "Authentication required"}` instead of Spring's default redirect.

### 2. New Discussion Placement + Delete Button (Requirement 2)

**Frontend changes:**
- Move "New Discussion" button to SpacePage header (inline with space name/description)
- Unauthenticated click → redirect to `/login` (check auth context before navigation)
- Add delete icon on posts where `canDelete` is true (backend already returns this based on author match)
- Delete flow: confirmation dialog → `DELETE /api/posts/{postId}` → optimistic removal with error rollback

### 3. Mobile-Responsive Layout (Requirement 3)

**CSS strategy using media queries at `max-width: 768px`:**
- Hide left sidebar and right sidebar
- Show `<BottomNav>` component with 4 icons (Home, Search, Create, Profile)
- Hide desktop top navigation
- All interactive elements: `min-height: 44px; min-width: 44px`
- All form inputs: `font-size: 16px` minimum (prevents iOS auto-zoom)
- Container: `overflow-x: hidden` to prevent horizontal scroll

### 4. Render Cold Start Keep-Alive (Requirement 4)

**External:** Configure cron-job.org to GET `https://<app>.onrender.com/actuator/health` every 14 minutes with 30s timeout and 1 retry.

**Backend:** Add `spring-boot-starter-actuator` dependency. Health endpoint is already permitted in SecurityConfig (public GET). Responds with `{"status": "UP"}`.

### 5. Clickable Navigation (Requirement 5)

**ForumCard component changes:**
- `w/spaceName` → `<Link to={`/w/${spaceName}`} onClick={e => e.stopPropagation()}>` 
- `@username` → `<Link to={`/users/${username}`} onClick={e => e.stopPropagation()}>`
- Tags → `<Link to={`/?tag=${tagName}`} onClick={e => e.stopPropagation()}>`
- `stopPropagation()` prevents the parent ForumCard's click-to-post-detail handler from firing
- All rendered as `<a>` elements (via react-router `<Link>`) — keyboard-focusable by default

### 6. Trending Tags → Feed (Requirement 6)

**FeedPage logic:**
```jsx
const [searchParams, setSearchParams] = useSearchParams();
const activeTag = searchParams.get("tag");

// Fetch based on tag presence
const endpoint = activeTag ? `/api/posts/tag/${activeTag}` : `/api/feed`;
```
- Active tag filter: show pill with tag name + ✕ clear button
- Clear button removes `tag` param from URL → triggers re-fetch of default feed
- Empty results: show "No posts found for #{tagName}" with filter still visible

### 7. Remove Blog Features (Requirement 7)

**Frontend only — no backend changes:**
- Remove "Blogs" from sidebar nav links
- Remove routes: `/blogs`, `/create-post/blog`, `/blog/:username`
- Add redirect: navigate to `/` for any removed blog route
- "Write" button → navigate directly to `/create-post/discussion` (no type picker)
- ProfilePage: remove "Blog Posts" tab, keep Discussions + Liked tabs
- Files to delete: `BlogsPage.jsx`, `CreateBlogPostPage.jsx`, `CreateBlogPostPage.module.css`

### 8. PWA Support (Requirement 8)

**vite-plugin-pwa configuration:**
```js
// vite.config.js
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: 'Yap',
        short_name: 'Yap',
        start_url: '/',
        display: 'standalone',
        theme_color: '#6C63FF',
        background_color: '#ffffff',
        icons: [
          { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png' }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        navigateFallback: '/offline.html'
      }
    })
  ]
})
```

**iOS meta tags in `index.html`:**
```html
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="default">
<link rel="apple-touch-icon" href="/icons/apple-touch-icon-180.png">
```

**Offline fallback:** Static `offline.html` page pre-cached by service worker, shown when network is unavailable.

---

## Phase 2 Design: Makes It Feel Real

### 9. Email Verification (Requirement 9)

**Flow:**
1. User registers → `AuthService.register()` generates 32+ char token via `SecureRandom`, stores on User with 24hr expiry
2. Send email via Resend HTTP API (`POST https://api.resend.com/emails`) with verification link: `${FRONTEND_URL}/verify?token={token}`
3. If Resend call fails → log error, registration still succeeds (user can resend later)
4. Frontend `/verify` route calls `POST /api/auth/verify-email?token={token}`
5. Backend validates: token exists, not expired, not already used → sets `emailVerified = true`, nullifies token
6. Resend endpoint: `POST /api/auth/resend-verification` — generates new token (invalidates old), rate-limited to 3/user/hour via simple DB count check

**Resend integration (no SDK, just RestClient):**
```java
restClient.post()
    .uri("https://api.resend.com/emails")
    .header("Authorization", "Bearer " + resendApiKey)
    .body(Map.of(
        "from", "Yap <noreply@yap.app>",
        "to", email,
        "subject", "Verify your Yap account",
        "html", verificationEmailHtml(token)
    ))
    .retrieve()
    .toBodilessEntity();
```

### 10. Password Reset (Requirement 10)

**Flow:**
1. `POST /api/auth/forgot-password` with `{email}` → always returns 200 (prevents email enumeration)
2. If email exists: generate reset token (32+ chars, `SecureRandom`), 1hr expiry, send link via Resend
3. Frontend `/reset-password?token={token}` shows password form
4. `POST /api/auth/reset-password` with `{token, newPassword}` → validate token, enforce min 8 chars, BCrypt hash, update password
5. Set `passwordChangedAt = now()` → JwtAuthFilter rejects tokens issued before this timestamp
6. Nullify reset token after use

**Session invalidation strategy:** Add `passwordChangedAt` field to User. In JwtAuthFilter, compare JWT's `iat` (issued-at) claim against `passwordChangedAt`. Reject if token was issued before password change.

### 11. Rate Limiting (Requirement 11)

**Implementation:** Bucket4j filter applied only to `/api/auth/login` and `/api/auth/register`.

```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService evictionScheduler = 
        Executors.newSingleThreadScheduledExecutor();
    
    // 5 tokens per minute per IP
    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
            .build();
    }
    
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

**Eviction:** Scheduled task runs every 5 minutes, removes entries inactive > 10 minutes (tracked via `lastAccessTime` wrapper).

**Response on limit exceeded:** HTTP 429, `Retry-After` header with seconds until next token, JSON body `{"error": "Too many requests. Try again later."}`.

### 12. Tag Input UX (Requirement 12)

**Frontend `<TagInput>` component:**
- Controlled input field listening for `keydown` (Enter or comma triggers)
- On trigger: `value.trim().toLowerCase()` → validate alphanumeric-only (`/^[a-z0-9]+$/`) and length ≤ 20
- Duplicate check: compare against existing pills (lowercased)
- Max 5 pills: disable input or show message at limit
- Each pill: `<span class="tag-pill">{tagName} <button onClick={remove}>✕</button></span>`
- Passes `string[]` to parent form component for submission

### 13. Mod Queue — Report-Driven (Requirement 13)

**Key design decision:** The mod queue shows posts/comments with PENDING reports, NOT toxicity-flagged content. There is no AI scoring.

**Backend:**
- `GET /api/mod/reports?page=0&size=20` — returns paginated reports with status=PENDING, ordered by `createdAt DESC`, includes post/comment details
- `PATCH /api/mod/reports/{reportId}/dismiss` — sets report status to DISMISSED
- `PATCH /api/mod/reports/{reportId}/remove` — sets report status to RESOLVED, sets target post's `isRemoved = true`
- Role check: `@PreAuthorize("hasAnyRole('ADMIN', 'MOD')")` on controller

**Frontend `/mod-queue` route:**
- Role gate: check user role on mount, redirect to `/` if not ADMIN/MOD
- Each card shows: post title, content preview, author, date, report reason, reporter, space
- Two action buttons: "Dismiss" (keeps content) and "Remove" (hides content)
- Optimistic UI: remove card from list on action, rollback on error

---

## Phase 3 Design: Growth & Retention

### 14. Smarter Feed Algorithm (Requirement 14)

**Scoring formula:**
```
baseScore = likeCount + (commentCount * 1.5)
hoursAge = hoursSince(post.createdAt)
decayFactor = 0.5 ^ (hoursAge / 24)    // halves every 24 hours
score = baseScore * decayFactor

// Velocity boost: if 5+ likes in last 60 min
if (recentLikes >= 5) score *= 2
```

**Query strategy:**
- Filter: `WHERE createdAt > now() - 7 days AND isDeleted = false AND isRemoved = false AND isFlagged = false`
- Compute score in application layer (not SQL) for flexibility
- Page size: 20 posts
- Discovery injection: for authenticated users, 1-2 random posts from spaces they don't follow (highest member_count spaces preferred)
- Unauthenticated: sort by `likeCount DESC` within last 7 days, no personalization

**Implementation:** `FeedService.getPersonalizedFeed(userId, page)` and `FeedService.getTrendingFeed(page)`.

### 15. Report Button (Requirement 15)

**Frontend:**
- "Report" button in post/comment overflow menu (⋯ three-dot)
- Only visible to authenticated users viewing content they didn't author
- Opens modal with reason selector (radio buttons for each `ReportReason` enum value) + optional details textarea (max 500 chars)
- Submits `POST /api/reports` with `{ postId?, commentId?, reason, details? }`

**Backend `ReportController`:**
- `POST /api/reports` — validates target exists, checks for duplicate (same user + same target), creates Report with status PENDING
- Duplicate check: `reportRepository.existsByReporterAndPost(user, post)` → 409 Conflict if exists
- Non-existent target → 404

### 16. GIF Picker (Requirement 16)

**Frontend `<GifPicker>` component:**
- Toggle button in post creation form opens picker popover/modal
- Initial state: fetch Giphy trending (`/v1/gifs/trending?api_key={key}&limit=25`)
- Search: 500ms debounce on input change → `/v1/gifs/search?api_key={key}&q={query}&limit=25`
- Grid layout of GIF thumbnails (use `images.fixed_width.url` for grid, store `images.original.url` for post)
- On select: close picker, show preview, store CDN URL in form state (`gifUrl`)
- Only 1 GIF per post: selecting replaces, remove button clears
- API key stored as Vite env var: `VITE_GIPHY_API_KEY`

### 17. Bookmarks (Requirement 17)

**Backend:**
- `BookmarkController`:
  - `POST /api/bookmarks/{postId}` — toggle: if bookmark exists → delete, else → create. Returns `{bookmarked: true/false}`
  - `GET /api/bookmarks?page=0&size=20` — user's bookmarks, ordered by `createdAt DESC`, excluding deleted/removed/flagged posts
- `PostSummary` DTO: add `isBookmarked` boolean field (populated when user is authenticated)

**Frontend:**
- Bookmark icon on each post (outlined = not saved, filled = saved)
- Click toggles with optimistic UI update
- `/saved` route: paginated list of bookmarked posts
- Unauthenticated → redirect to `/login` on bookmark click or `/saved` navigation

### 18. Search (Requirement 18)

**Backend `SearchController`:**
- `GET /api/search?q={query}&type={posts|spaces|users}&page=0&size=20`
- Query validation: minimum 2 characters, max 100 characters
- PostgreSQL ILIKE queries per type:

```sql
-- Posts (type=posts)
SELECT * FROM posts 
WHERE (title ILIKE '%query%' OR content_text ILIKE '%query%') 
  AND is_deleted = false AND is_removed = false AND is_flagged = false
ORDER BY created_at DESC

-- Spaces (type=spaces)
SELECT * FROM communities 
WHERE (name ILIKE '%query%' OR description ILIKE '%query%')
ORDER BY member_count DESC

-- Users (type=users)
SELECT * FROM users 
WHERE username ILIKE '%query%' AND is_banned = false AND is_active = true
ORDER BY follower_count DESC
```

**Frontend:**
- Search input in top nav (desktop) and bottom nav Search tab (mobile)
- Results page with category tabs: Posts | Spaces | Users
- Default tab: Posts
- Empty state per tab when no results

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Public GET access permits unauthenticated requests

*For any* valid public GET endpoint path (feed, posts, spaces, profiles), an unauthenticated request SHALL receive a successful response (not 401), while the same path with a non-GET method SHALL receive 401.

**Validates: Requirements 1.1, 1.2**

### Property 2: Non-GET requests require authentication

*For any* HTTP method in {POST, PUT, PATCH, DELETE} and any endpoint path not matching `/api/auth/**`, an unauthenticated request SHALL receive HTTP 401.

**Validates: Requirements 1.2, 1.4**

### Property 3: Delete button visibility matches canDelete flag

*For any* post rendered in the UI, the delete button SHALL be visible if and only if `canDelete` is true for that post.

**Validates: Requirements 2.3**

### Property 4: ForumCard link navigation does not trigger parent click

*For any* space name, username, or tag name rendered on a ForumCard, clicking that link SHALL navigate to the correct target route (`/w/{spaceName}`, `/users/{username}`, or `/?tag={tagName}`) without triggering the ForumCard's post-detail navigation.

**Validates: Requirements 5.1, 5.2, 5.3**

### Property 5: Tag filter controls feed endpoint selection

*For any* non-empty tag name in the URL query parameter, the FeedPage SHALL call the tag-specific endpoint; when the tag parameter is absent or empty, the FeedPage SHALL call the default feed endpoint.

**Validates: Requirements 6.1, 6.3**

### Property 6: Verification token single-use round trip

*For any* valid verification token submitted to the verify endpoint, the first submission SHALL succeed (marking email verified) and any subsequent submission of the same token SHALL be rejected with HTTP 400.

**Validates: Requirements 9.3, 9.4**

### Property 7: Password reset does not reveal email registration status

*For any* email address (registered or not) submitted to the forgot-password endpoint, the response status and structure SHALL be identical, preventing email enumeration.

**Validates: Requirements 10.1**

### Property 8: Password reset invalidates prior sessions

*For any* user who completes a password reset, all JWT tokens issued before the reset timestamp SHALL be rejected by the auth filter.

**Validates: Requirements 10.5**

### Property 9: Password policy enforcement on reset

*For any* password string shorter than 8 characters submitted during password reset, the request SHALL be rejected with HTTP 400. For any password string of 8 or more characters, the password requirement SHALL be satisfied.

**Validates: Requirements 10.6**

### Property 10: Rate limiter blocks 6th request within one minute

*For any* IP address sending requests to `/api/auth/login` or `/api/auth/register`, the 6th request within a 1-minute window SHALL receive HTTP 429 with a valid `Retry-After` header.

**Validates: Requirements 11.1, 11.2, 11.3**

### Property 11: Rate limiter resolves IP from X-Forwarded-For

*For any* request with an `X-Forwarded-For` header, the rate limiter SHALL use the first IP value in that header for bucket resolution, and different first-IP values SHALL be tracked independently.

**Validates: Requirements 11.4**

### Property 12: Rate limiter does not affect non-auth endpoints

*For any* endpoint path not matching `/api/auth/login` or `/api/auth/register`, sending any number of requests SHALL never result in HTTP 429 from the rate limiter.

**Validates: Requirements 11.6**

### Property 13: Tag input normalization and validation

*For any* input string, the tag creation logic SHALL produce a trimmed, lowercased result containing only alphanumeric characters, rejecting inputs that are empty after trimming, contain non-alphanumeric characters, or exceed 20 characters.

**Validates: Requirements 12.1, 12.5**

### Property 14: Tag uniqueness and maximum count invariant

*For any* sequence of tag additions, the resulting tag list SHALL contain no duplicate values (compared case-insensitively) and SHALL never exceed 5 items.

**Validates: Requirements 12.2, 12.4**

### Property 15: Mod queue access control

*For any* user with a role other than ADMIN or MOD, accessing the mod queue endpoint SHALL be denied (redirect on frontend, 403 on backend).

**Validates: Requirements 13.1, 13.7**

### Property 16: Mod queue displays only PENDING reports ordered by date

*For any* set of reports in the database, the mod queue SHALL return only those with status PENDING, ordered by report creation date descending.

**Validates: Requirements 13.2**

### Property 17: Feed excludes removed/deleted/flagged posts

*For any* post where `isDeleted`, `isRemoved`, or `isFlagged` is true, that post SHALL never appear in any feed response (authenticated or unauthenticated).

**Validates: Requirements 14.5**

### Property 18: Feed time-decay halves score every 24 hours

*For any* post, its ranking score at age T+24h SHALL be approximately half its score at age T (all other factors being equal).

**Validates: Requirements 14.1**

### Property 19: Feed only includes posts from last 7 days

*For any* post with `createdAt` older than 7 days, that post SHALL not appear in feed results.

**Validates: Requirements 14.2**

### Property 20: Report button visibility excludes own content

*For any* authenticated user viewing a post or comment, the report button SHALL be visible if and only if the content was not authored by that user.

**Validates: Requirements 15.1**

### Property 21: Duplicate report rejection

*For any* user who has already submitted a report for a specific post or comment, a second report submission for the same target SHALL be rejected regardless of the reason selected.

**Validates: Requirements 15.4**

### Property 22: Bookmark toggle round trip

*For any* authenticated user and post, bookmarking then un-bookmarking SHALL result in no bookmark record existing, and the bookmark icon SHALL return to the outlined state.

**Validates: Requirements 17.1, 17.2**

### Property 23: Saved posts exclude deleted/removed/flagged content

*For any* bookmarked post that is subsequently deleted, removed, or flagged, that post SHALL not appear in the user's saved posts list.

**Validates: Requirements 17.3**

### Property 24: Search results match query substring (case-insensitive)

*For any* search query of 2+ characters and any result returned, the result's searchable field(s) SHALL contain the query as a case-insensitive substring.

**Validates: Requirements 18.2, 18.3**

### Property 25: Search excludes banned/inactive users and removed/deleted posts

*For any* search query, no result SHALL include a user where `isBanned` is true or `isActive` is false, and no result SHALL include a post where `isDeleted`, `isRemoved`, or `isFlagged` is true.

**Validates: Requirements 18.4**

---

## Error Handling

### Backend Error Responses

All error responses follow a consistent JSON structure:
```json
{
  "error": "Human-readable error message",
  "status": 400
}
```

| Scenario | Status | Message |
|----------|--------|---------|
| Unauthenticated mutating request | 401 | "Authentication required" |
| Rate limit exceeded | 429 | "Too many requests. Try again later." |
| Invalid/expired verification token | 400 | "Token is invalid or expired" |
| Invalid/expired reset token | 400 | "Token is invalid or expired" |
| Password too short on reset | 400 | "Password must be at least 8 characters" |
| Duplicate report | 409 | "You have already reported this content" |
| Report target not found | 404 | "Post not found" or "Comment not found" |
| Delete post not owned | 403 | "You can only delete your own posts" |
| Mod action unauthorized | 403 | "Insufficient permissions" |
| Search query too short | 400 | "Search query must be at least 2 characters" |
| Resend verification rate exceeded | 429 | "Verification email limit reached. Try again later." |

### Frontend Error Handling

- API errors: display toast/inline message with backend error text
- Network errors: show "Something went wrong. Check your connection."
- Offline: service worker serves cached fallback page
- Optimistic UI rollback: if mutation fails, revert local state and show error

---

## Testing Strategy

### Unit Tests (Example-Based)

Focus areas:
- SecurityConfig: specific endpoint access patterns (public GET, auth-required mutations)
- Token generation: verify length and randomness properties
- Password policy validation: boundary cases (7 chars fails, 8 chars passes)
- Feed algorithm: specific scoring scenarios with known inputs
- Tag normalization: specific input/output pairs

### Property-Based Tests

**Library:** jqwik (Java) for backend, fast-check (JavaScript) for frontend logic

**Configuration:** Minimum 100 iterations per property test.

Property tests should cover:
- Rate limiter behavior across random IP/request sequences (Properties 10-12)
- Tag input normalization across random strings (Properties 13-14)
- Feed algorithm time-decay correctness across random post ages (Property 18)
- Search result filtering across random data states (Properties 24-25)
- Token single-use enforcement across random submission sequences (Property 6)
- Password policy across random string lengths (Property 9)

**Tag format:** `Feature: yap-v1-launch, Property {number}: {property_text}`

### Integration Tests

- Email sending via Resend (mocked in test, real in staging)
- Giphy API search and trending (mocked)
- cron-job.org keep-alive (manual verification)
- PWA: Lighthouse audit for installability

### Manual/Visual Tests

- Mobile responsive layout at 768px breakpoint
- PWA install flow on iOS and Android
- Offline fallback page
- Bottom navigation UX
- GIF picker usability

---

## Security Considerations

1. **CORS:** Strictly restricted to production Vercel URL + localhost. No wildcards.
2. **Rate limiting:** Bucket4j on auth endpoints (5/min/IP) prevents brute force and credential stuffing
3. **Input validation:** Jakarta validation annotations on all DTOs (existing)
4. **JWT + BCrypt:** Existing authentication with password hashing
5. **No AI moderation:** Moderation is human-only via report queue — avoids false positives on sensitive discussions
6. **Token security:** Verification and reset tokens are cryptographically random (SecureRandom), time-limited, single-use
7. **Email enumeration prevention:** Password reset always returns 200 regardless of email existence
8. **Session invalidation:** Password changes invalidate all prior JWT tokens via `passwordChangedAt` comparison
9. **Role-based access:** Mod queue restricted to ADMIN/MOD roles via `@PreAuthorize`
10. **Public read / authenticated write:** Prevents anonymous spam while allowing content discovery
11. **IP resolution:** X-Forwarded-For parsing for correct rate limiting behind Render's proxy
12. **Eviction:** Rate limit buckets evicted after 10 min inactivity to prevent memory exhaustion from attacks

---

## Dependencies

| Dependency | Purpose | Cost |
|---|---|---|
| spring-boot-starter-actuator | Health endpoint for keep-alive | Free (bundled) |
| bucket4j-core | In-memory rate limiting | Free (OSS) |
| vite-plugin-pwa | Service worker + manifest generation | Free (OSS) |
| Resend | Transactional email (verification, password reset) | Free tier (100/day) |
| Giphy API | GIF search and trending | Free tier |
| cron-job.org | Keep-alive pings every 14 min | Free |
| Cloudinary | Image uploads (existing) | Free tier |

**Removed from previous design:**
- ~~Perspective API~~ — No AI moderation
- ~~spring-boot-starter-webflux~~ — No longer needed (was for async Perspective calls). Use Spring's `RestClient` for Resend HTTP calls instead.

---

## Constraints & Trade-offs

| Constraint | Impact | Mitigation |
|---|---|---|
| No Redis | Rate limit state lost on restart; not shared across instances | Acceptable: single Render instance, restarts are rare with keep-alive |
| No Elasticsearch | Search uses ILIKE — O(n) full table scan | Fine for < 50k posts; add `pg_trgm` GIN index if needed later |
| Resend 100/day limit | Caps verification + reset emails | Sufficient for launch (20-50 testers); upgrade plan when growing |
| Single Render dyno | No horizontal scaling, 512MB RAM | Keep-alive prevents cold starts; Bucket4j eviction prevents memory bloat |
| No AI moderation | Toxic content visible until reported and manually reviewed | Acceptable for v1 — founder monitors actively; community self-polices via reports |
| PostgreSQL ILIKE | Case-insensitive but no relevance ranking | Adequate for keyword search at v1 scale; full-text search (tsvector) is a future upgrade |
| In-memory rate limiting | DDoS with millions of unique IPs could exhaust memory | 10-min eviction + single-dyno restart as circuit breaker; Render's own DDoS protection helps |
| Free Giphy tier | Rate limited, watermarked in some contexts | Acceptable for v1 UX; can upgrade or switch providers later |
