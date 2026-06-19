# Implementation Plan: Yap V1 Launch

## Overview

This plan implements the 18 requirements across three phases for Yap's V1 launch. The backend is Java Spring Boot 4.x with PostgreSQL on Render; the frontend is React + Vite on Vercel. All services stay within the $0 budget using free tiers. Implementation proceeds phase-by-phase: Phase 1 (pre-launch blockers), Phase 2 (production-ready feel), Phase 3 (growth & retention).

## Tasks

- [x] 1. Phase 1 — Backend: Public read access, keep-alive, and security config
  - [x] 1.1 Update SecurityConfig for public GET access and custom AuthenticationEntryPoint
    - Modify `SecurityConfig.filterChain()` to permit unauthenticated GET on `/api/**` except `/api/users/me` and `/api/spaces/my`
    - Require authentication for all POST/PUT/PATCH/DELETE except `/api/auth/**`
    - Permit `/actuator/health` without auth
    - Add CORS configuration reading `FRONTEND_URL` env var, defaulting to `http://localhost:5173`
    - Implement custom `AuthenticationEntryPoint` returning JSON `{"error": "Authentication required"}` with 401 status
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [x] 1.2 Add Spring Boot Actuator dependency and verify health endpoint
    - Add `spring-boot-starter-actuator` to `pom.xml`
    - Verify `/actuator/health` responds with `{"status": "UP"}` on public GET
    - _Requirements: 4.3, 4.4_

  - [x] 1.3 Write property tests for public GET access and auth-required mutations
    - **Property 1: Public GET access permits unauthenticated requests**
    - **Property 2: Non-GET requests require authentication**
    - **Validates: Requirements 1.1, 1.2, 1.4**

- [x] 2. Phase 1 — Frontend: Blog removal, new discussion placement, delete button
  - [x] 2.1 Remove blog features from frontend
    - Delete `BlogsPage.jsx`, `CreateBlogPostPage.jsx`, `CreateBlogPostPage.module.css` (and related files)
    - Remove "Blogs" nav item from sidebar
    - Remove routes: `/blogs`, `/create-post/blog`, `/blog/:username`
    - Add redirects for removed blog routes → `/`
    - "Write" button navigates directly to `/create-post/discussion`
    - Remove "Blog Posts" tab from ProfilePage, keep Discussions + Liked tabs
    - _Requirements: 7.1, 7.2, 7.3, 7.5, 7.6_

  - [x] 2.2 Move "New Discussion" button and add delete button on posts
    - Move "New Discussion" button to SpacePage header (inline with space name)
    - Unauthenticated click → redirect to `/login`
    - Add delete icon on posts where `canDelete` is true
    - Implement confirmation dialog on delete click
    - Call `DELETE /api/posts/{postId}` on confirm, optimistic removal with error rollback
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ]* 2.3 Write property test for delete button visibility
    - **Property 3: Delete button visibility matches canDelete flag**
    - **Validates: Requirements 2.3**

- [x] 3. Phase 1 — Frontend: Clickable navigation links on ForumCard
  - [x] 3.1 Make space names, usernames, and tags clickable links on ForumCard
    - `w/spaceName` → `<Link to={/w/${spaceName}}>` with `stopPropagation()`
    - `@username` → `<Link to={/users/${username}}>` with `stopPropagation()`
    - Tags → `<Link to={/?tag=${tagName}}>` with `stopPropagation()`
    - Style links with distinct color and hover underline/pointer
    - Ensure keyboard-focusable and Enter-activatable (native anchor behavior)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

  - [ ]* 3.2 Write property test for ForumCard link navigation
    - **Property 4: ForumCard link navigation does not trigger parent click**
    - **Validates: Requirements 5.1, 5.2, 5.3**

- [x] 4. Phase 1 — Frontend: Wire trending tags to feed
  - [x] 4.1 Implement tag-based feed filtering on FeedPage
    - Read `tag` query param via `useSearchParams()`
    - If `tag` param present: call `/api/posts/tag/{tagName}` endpoint
    - If absent/empty: call default feed endpoint
    - Show active tag pill with tag name + ✕ clear button
    - Clear button removes `tag` param → re-fetches default feed
    - Empty results: display "No posts found for #{tagName}" with filter visible
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [ ]* 4.2 Write property test for tag filter feed endpoint selection
    - **Property 5: Tag filter controls feed endpoint selection**
    - **Validates: Requirements 6.1, 6.3**

- [x] 5. Phase 1 — Frontend: Mobile-responsive layout and PWA
  - [x] 5.1 Implement mobile-responsive CSS and BottomNav component
    - Add viewport meta tag `width=device-width, initial-scale=1` (verify in index.html)
    - Create `<BottomNav>` component with Home, Search, Create, Profile icons
    - Add media queries at `max-width: 768px`: hide sidebars, show BottomNav, hide desktop nav
    - Set `min-height: 44px; min-width: 44px` on interactive elements for mobile
    - Set `font-size: 16px` minimum on inputs/textareas/selects
    - Set `overflow-x: hidden` on container for mobile
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [x] 5.2 Configure vite-plugin-pwa and add offline fallback
    - Install `vite-plugin-pwa`
    - Configure manifest in `vite.config.js` (name: "Yap", start_url: "/", display: "standalone", theme_color, icons 192/512)
    - Add iOS meta tags in `index.html` (apple-mobile-web-app-capable, apple-touch-icon 180px)
    - Create `offline.html` fallback page (pre-cached by service worker)
    - Configure workbox `navigateFallback` to offline.html
    - Create/add placeholder icon files (192, 512, apple-touch-icon 180)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

- [ ] 6. Checkpoint — Phase 1 complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Phase 2 — Backend: Email verification and password reset
  - [ ] 7.1 Add email verification fields to User entity and implement AuthService verification flow
    - Add fields to User: `emailVerified`, `verificationToken`, `verificationTokenExpiry`
    - On register: generate 32+ char token via SecureRandom, set 24hr expiry
    - Send verification email via Resend REST API (RestClient POST to `https://api.resend.com/emails`)
    - If Resend fails: log error, registration still succeeds
    - Implement `POST /api/auth/verify-email?token={token}` — validate token exists, not expired, not used → set emailVerified=true, nullify token
    - Invalid/expired token → 400 with error message
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ] 7.2 Implement resend verification endpoint
    - `POST /api/auth/resend-verification` — generates new token (invalidates old), sends email
    - Rate limit: 3 resend requests per user per hour (DB count check on token generation timestamps)
    - _Requirements: 9.6_

  - [ ] 7.3 Implement password reset flow
    - Add fields to User: `resetToken`, `resetTokenExpiry`, `passwordChangedAt`
    - `POST /api/auth/forgot-password` — always returns 200 (no email enumeration)
    - If email exists: generate reset token (32+ chars, 1hr expiry), send link via Resend
    - `POST /api/auth/reset-password` — validate token, enforce min 8 chars, BCrypt hash, update password
    - Set `passwordChangedAt = now()`, nullify reset token
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.6_

  - [ ] 7.4 Implement JWT session invalidation on password change
    - In JwtAuthFilter: compare JWT `iat` claim against user's `passwordChangedAt`
    - Reject tokens issued before password change timestamp
    - _Requirements: 10.5_

  - [ ]* 7.5 Write property tests for email verification and password reset
    - **Property 6: Verification token single-use round trip**
    - **Property 7: Password reset does not reveal email registration status**
    - **Property 8: Password reset invalidates prior sessions**
    - **Property 9: Password policy enforcement on reset**
    - **Validates: Requirements 9.3, 9.4, 10.1, 10.5, 10.6**

- [ ] 8. Phase 2 — Backend: Rate limiting
  - [ ] 8.1 Implement Bucket4j rate limiter filter for auth endpoints
    - Add `bucket4j-core` dependency to `pom.xml`
    - Create `RateLimitFilter extends OncePerRequestFilter`
    - Apply only to `/api/auth/login` and `/api/auth/register`
    - 5 tokens per minute per IP (ConcurrentHashMap-based bucket storage)
    - Resolve client IP from `X-Forwarded-For` header (first value), fallback to remoteAddr
    - On limit exceeded: HTTP 429 with `Retry-After` header and JSON error body
    - Scheduled eviction: remove entries inactive > 10 minutes (runs every 5 min)
    - Register filter in SecurityConfig filter chain
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_

  - [ ]* 8.2 Write property tests for rate limiter
    - **Property 10: Rate limiter blocks 6th request within one minute**
    - **Property 11: Rate limiter resolves IP from X-Forwarded-For**
    - **Property 12: Rate limiter does not affect non-auth endpoints**
    - **Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.6**

- [ ] 9. Phase 2 — Frontend: Tag input pills and verification banner
  - [ ] 9.1 Implement TagInput component with pill UX
    - Create `<TagInput>` component with controlled input
    - On Enter or comma keydown: trim, lowercase, validate alphanumeric-only (`/^[a-z0-9]+$/`) and length ≤ 20
    - Duplicate check (case-insensitive) → reject silently
    - Max 5 pills: disable input and show message at limit
    - Each pill shows tag name + ✕ remove button
    - Remove button removes tag and re-enables input if at limit
    - Pass `string[]` to parent form
    - Integrate into post creation form
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [ ]* 9.2 Write property tests for tag input normalization and validation
    - **Property 13: Tag input normalization and validation**
    - **Property 14: Tag uniqueness and maximum count invariant**
    - **Validates: Requirements 12.1, 12.2, 12.4, 12.5**

  - [ ] 9.3 Add email verification reminder banner on frontend
    - Show dismissible banner at top of every page when user's email is unverified
    - Include "Resend verification" link that calls `POST /api/auth/resend-verification`
    - Display success/error feedback
    - _Requirements: 9.7_

- [ ] 10. Phase 2 — Backend + Frontend: Mod queue (report-driven)
  - [ ] 10.1 Implement mod queue backend endpoints
    - `GET /api/mod/reports?page=0&size=20` — paginated PENDING reports ordered by createdAt DESC, include post/comment details
    - `PATCH /api/mod/reports/{reportId}/dismiss` — set status DISMISSED
    - `PATCH /api/mod/reports/{reportId}/remove` — set status RESOLVED, set target post `isRemoved = true`
    - Add `@PreAuthorize("hasAnyRole('ADMIN', 'MOD')")` on controller methods
    - Return 403 for unauthorized access
    - _Requirements: 13.2, 13.3, 13.4, 13.5, 13.7_

  - [ ] 10.2 Implement mod queue frontend page
    - Create `/mod-queue` route, role-gate on mount (redirect to `/` if not ADMIN/MOD)
    - Display cards: post title, content preview, author, date, report reason, reporter, space
    - "Dismiss" button → PATCH dismiss endpoint, optimistic removal from list
    - "Remove" button → PATCH remove endpoint, optimistic removal from list
    - Error rollback: show error message, keep post in list
    - _Requirements: 13.1, 13.3, 13.4, 13.5, 13.6, 13.7_

  - [ ]* 10.3 Write property tests for mod queue access control and display
    - **Property 15: Mod queue access control**
    - **Property 16: Mod queue displays only PENDING reports ordered by date**
    - **Validates: Requirements 13.1, 13.2, 13.7**

- [ ] 11. Checkpoint — Phase 2 complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 12. Phase 3 — Backend: Feed algorithm
  - [ ] 12.1 Implement FeedService with time-decay scoring and discovery injection
    - Create `FeedService` with `getPersonalizedFeed(userId, page)` and `getTrendingFeed(page)`
    - Filter: posts from last 7 days, not deleted/removed/flagged
    - Scoring: `baseScore = likeCount + (commentCount * 1.5)`, `decayFactor = 0.5 ^ (hoursAge / 24)`, `score = baseScore * decayFactor`
    - Velocity boost: if 5+ likes in last 60 min → score *= 2
    - Discovery: inject 1-2 posts from non-followed spaces (highest member_count)
    - Unauthenticated: sort by likeCount DESC within last 7 days, no personalization
    - Page size: 20 posts
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

  - [ ]* 12.2 Write property tests for feed algorithm
    - **Property 17: Feed excludes removed/deleted/flagged posts**
    - **Property 18: Feed time-decay halves score every 24 hours**
    - **Property 19: Feed only includes posts from last 7 days**
    - **Validates: Requirements 14.1, 14.2, 14.5**

- [ ] 13. Phase 3 — Backend + Frontend: Report button
  - [ ] 13.1 Implement ReportController for user-submitted reports
    - `POST /api/reports` with `{ postId?, commentId?, reason, details? }`
    - Validate target exists (404 if not)
    - Duplicate check: `reportRepository.existsByReporterAndPost(user, post)` → 409 if exists
    - Create Report with status PENDING
    - _Requirements: 15.3, 15.4, 15.6_

  - [ ] 13.2 Implement report button and form on frontend
    - Add "Report" option in post/comment overflow menu (⋯ three-dot)
    - Only visible to authenticated users viewing content not authored by them
    - Modal with radio buttons for each ReportReason + optional details textarea (max 500 chars)
    - Validate reason selected before submit
    - Show confirmation message on success
    - _Requirements: 15.1, 15.2, 15.5, 15.7_

  - [ ]* 13.3 Write property tests for report functionality
    - **Property 20: Report button visibility excludes own content**
    - **Property 21: Duplicate report rejection**
    - **Validates: Requirements 15.1, 15.4**

- [ ] 14. Phase 3 — Frontend: GIF picker
  - [ ] 14.1 Implement GifPicker component in post creation form
    - Create `<GifPicker>` component with toggle button in post creation form
    - Initial state: fetch Giphy trending (`/v1/gifs/trending?api_key={key}&limit=25`)
    - Search: 500ms debounce on input → Giphy search API, display up to 25 results in grid
    - On select: close picker, show animated preview, store `images.original.url` in form `gifUrl` field
    - Only 1 GIF per post: selecting replaces previous
    - Remove button clears `gifUrl` and preview
    - API key via `VITE_GIPHY_API_KEY` env var
    - Handle API errors with inline message
    - Display GIF in posts with max-width 400px, preserving aspect ratio
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8_

- [ ] 15. Phase 3 — Backend + Frontend: Bookmarks
  - [ ] 15.1 Create Bookmark entity and BookmarkController
    - Create `Bookmark` entity with unique constraint on (user_id, post_id)
    - `POST /api/bookmarks/{postId}` — toggle: exists → delete, else → create. Returns `{bookmarked: true/false}`
    - `GET /api/bookmarks?page=0&size=20` — user's bookmarks ordered by createdAt DESC, exclude deleted/removed/flagged posts
    - Add `isBookmarked` field to PostSummary DTO (populated when user is authenticated)
    - _Requirements: 17.1, 17.2, 17.3, 17.5_

  - [ ] 15.2 Implement bookmark UI on frontend
    - Add bookmark icon on each post (outlined = not saved, filled = saved)
    - Click toggles with optimistic UI update
    - Create `/saved` route: paginated list of bookmarked posts
    - Empty state message when no saved posts
    - Unauthenticated → redirect to `/login` on bookmark click or `/saved` navigation
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7_

  - [ ]* 15.3 Write property tests for bookmarks
    - **Property 22: Bookmark toggle round trip**
    - **Property 23: Saved posts exclude deleted/removed/flagged content**
    - **Validates: Requirements 17.1, 17.2, 17.3**

- [ ] 16. Phase 3 — Backend + Frontend: Search
  - [ ] 16.1 Implement SearchController with ILIKE queries
    - `GET /api/search?q={query}&type={posts|spaces|users}&page=0&size=20`
    - Query validation: min 2 chars, max 100 chars
    - Posts: ILIKE on title + content_text, exclude deleted/removed/flagged, order by created_at DESC
    - Spaces: ILIKE on name + description, order by member_count DESC
    - Users: ILIKE on username, exclude banned/inactive, order by follower_count DESC
    - _Requirements: 18.2, 18.3, 18.4_

  - [ ] 16.2 Implement search UI on frontend
    - Search input in top nav (desktop) and via Search tab in BottomNav (mobile)
    - Results page with category tabs: Posts | Spaces | Users (default: Posts)
    - Validate min 2 chars on client before submitting
    - Empty state per tab when no results
    - _Requirements: 18.1, 18.5, 18.6, 18.7_

  - [ ]* 16.3 Write property tests for search
    - **Property 24: Search results match query substring (case-insensitive)**
    - **Property 25: Search excludes banned/inactive users and removed/deleted posts**
    - **Validates: Requirements 18.2, 18.3, 18.4**

- [ ] 17. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation after each phase
- Property tests validate universal correctness properties from the design doc (jqwik for Java backend, fast-check for frontend)
- The keep-alive cron job (Requirement 4.1, 4.2) is configured externally on cron-job.org — no code task needed beyond exposing the health endpoint (task 1.2)
- Backend retains `PostType.BLOG` enum and schema (Requirement 7.4) — no backend changes needed for blog removal
- Icon asset creation (192px, 512px, 180px apple-touch-icon) may require design input from the user

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "2.1"] },
    { "id": 1, "tasks": ["1.3", "2.2", "3.1", "5.1"] },
    { "id": 2, "tasks": ["2.3", "3.2", "4.1", "5.2"] },
    { "id": 3, "tasks": ["4.2", "7.1", "8.1"] },
    { "id": 4, "tasks": ["7.2", "7.3", "8.2", "9.1"] },
    { "id": 5, "tasks": ["7.4", "9.2", "9.3", "10.1"] },
    { "id": 6, "tasks": ["7.5", "10.2"] },
    { "id": 7, "tasks": ["10.3", "12.1"] },
    { "id": 8, "tasks": ["12.2", "13.1"] },
    { "id": 9, "tasks": ["13.2", "14.1", "15.1"] },
    { "id": 10, "tasks": ["13.3", "15.2", "16.1"] },
    { "id": 11, "tasks": ["15.3", "16.2"] },
    { "id": 12, "tasks": ["16.3"] }
  ]
}
```
