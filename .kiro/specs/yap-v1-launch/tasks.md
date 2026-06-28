# Implementation Plan: Yap V1 Launch

## Overview

This plan implements the 18 requirements across three phases for Yap's V1 launch. The backend is Java Spring Boot 4.x on Render; the database is PostgreSQL on Neon; the frontend is React + Vite on Vercel. Additional services: Resend (email), Giphy (GIFs), Cloudinary (avatar uploads). All services stay within the $0 budget using free tiers. Implementation proceeds phase-by-phase: Phase 1 (pre-launch blockers), Phase 2 (production-ready feel), Phase 3 (growth & retention).

Key design decisions: mobile 3-item bottom nav (Home | Explore | Profile), combined Explore page at `/explore` (trending + hot + popular spaces + search), post flair replaces tag UI, feed algorithm with affinity scoring + diversity + discovery injection, expanded rate limiting (single configurable filter), reports + mod queue together in Phase 2, bookmarks in Phase 2, no member gate for posting, GIF picker in posts AND comments, PWA install nudge, desktop search in topbar → /explore?q=, profile at /@:username.

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
    - `@username` → `<Link to={/@${username}}>` with `stopPropagation()`
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
    - Create `<BottomNav>` component with Home, Explore, Profile icons
    - Add media queries at `max-width: 768px`: hide sidebars, show BottomNav, hide desktop nav
    - Set `min-height: 44px; min-width: 44px` on interactive elements for mobile
    - Set `font-size: 16px` minimum on inputs/textareas/selects
    - Set `overflow-x: hidden` on container for mobile
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [x] 5.2 Configure vite-plugin-pwa and add offline fallback
    - Install `vite-plugin-pwa`
    - Configure manifest in `vite.config.js` (name: "Yap", start_url: "/", display: "standalone", theme_color: "#C4973F", background_color: "#13151F", icons 192/512)
    - Add iOS meta tags in `index.html` (apple-mobile-web-app-capable, apple-touch-icon 180px, apple-mobile-web-app-status-bar-style: black-translucent)
    - Create `offline.html` fallback page (pre-cached by service worker)
    - Configure workbox `navigateFallback` to offline.html
    - Create/add placeholder icon files (192, 512, apple-touch-icon 180)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

- [x] 6. Checkpoint — Phase 1 complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Phase 2 — Backend: Expanded rate limiting + security hardening
  - [x] 7.1 Implement single configurable RateLimitFilter
    - Add `bucket4j-core` dependency to `pom.xml`
    - Create `RateLimitFilter extends OncePerRequestFilter` with a configuration map:
      - `/api/auth/login`: 5/min/IP
      - `/api/auth/register`: 5/min/IP
      - `POST /api/posts`: 5/min/user (requires extracting userId from JWT)
      - `POST /api/posts/*/like`, `DELETE /api/posts/*/like`: 30/min/user
      - `POST /api/posts/*/comments`: 10/min/user
      - `POST /api/reports`: 3/min/user
      - `POST /api/users/*/follow`, `DELETE /api/users/*/unfollow`: 10/min/user
      - `GET /api/**` (unauthenticated): 120/min/IP
    - IP resolution: X-Forwarded-For first value, fallback remoteAddr
    - User resolution: extract from SecurityContext for user-based limits
    - On limit exceeded: HTTP 429, Retry-After header, JSON error body
    - Scheduled eviction every 5 min (entries inactive > 10 min)
    - Register in SecurityConfig filter chain
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_

  - [x] 7.2 Add security hardening configuration
    - Set `server.tomcat.max-http-form-post-size=51200` (50KB) in application.properties
    - Set `spring.servlet.multipart.max-file-size=10MB` for avatar uploads
    - Set `server.connection-timeout=5000` (5 seconds)
    - Cap page size at 50 in all paginated endpoints (add validation in controller/service layer)
    - Add isBanned check in JwtAuthFilter — if user.isBanned, return 403 on all requests
    - Verify @Size constraints: PostCreate.title(300), PostCreate.contentText(5000), CommentCreate.contentText(2000), ReportRequest.details(500)
    - _Requirements: 11.5 (partial), new security requirements_

  - [x] 7.3 Write property tests for rate limiter + security
    - **Property 10: Rate limiter blocks request exceeding configured limit per path**
    - **Property 11: Rate limiter resolves IP from X-Forwarded-For**
    - **Property 12: Rate limiter applies user-based limits using JWT identity**
    - **Property 13: Banned user receives 403 on all endpoints**
    - **Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.6**

- [x] 8. Phase 2 — Backend: Post flair + feed algorithm + hot posts
  - [x] 8.1 Add flair to posts
    - Flyway migration: add `flair` varchar(50) nullable to posts table
    - Add `flair` field to Post entity, PostSummary DTO, PostCreate DTO
    - No DB enum — validation happens frontend-side
    - _Requirements: new (post flair)_

  - [x] 8.2 Implement FeedService with scoring + affinity + diversity
    - Create FeedService.java with methods:
      - `getPersonalizedFeed(userId, page)`: posts from last 30 days (configurable via `app.feed.window-days=30` in application.properties), from joined spaces ranked by score. Affinity multiplier: spaces where user has liked/commented get 1.5× weight. Diversity: max 2 consecutive posts from same space. Discovery injection at positions 5 and 12 from highest-memberCount non-joined spaces
      - `getPublicFeed(page)`: all posts ranked by score (logged-out users, cold-start users with <3 joined spaces get 70% public + 30% personalized)
      - `getHotPosts()`: top 5 by score, last 48h, no pagination
    - Scoring: `baseScore = likeCount + (commentCount × 1.5)`, `decayFactor = 0.5^(hoursAge/24)`, `score = baseScore × decayFactor`
    - Velocity boost: 5+ likes in last 60 min → score × 2
    - Filter: exclude isDeleted, isRemoved, isFlagged
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

  - [x] 8.3 New feed and hot posts endpoints
    - `GET /api/posts/feed?page=0&size=20` — personalized feed (uses JWT userId) or public feed if unauthenticated
    - `GET /api/posts/hot` — returns List<PostSummary>, top 5
    - `GET /api/posts/public?page=0&size=20` — all public posts ranked by score (for Explore page)
    - Add `authorAvatarUrl` field to PostSummary DTO (for avatar display on cards)
    - Verify Post → PostSummary mapper populates `authorAvatarUrl` from the User entity's avatarUrl field
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

  - [x] 8.4 Write property tests for feed algorithm
    - **Property 14: Feed excludes removed/deleted/flagged posts**
    - **Property 15: Feed time-decay halves score every 24 hours**
    - **Property 16: Feed diversity — max 2 consecutive posts from same space**
    - **Property 17: Cold start users get public feed blend**
    - **Validates: Requirements 14.1, 14.2, 14.5**

- [x] 9. Phase 2 — Backend + Frontend: Report system + Mod queue
  - [x] 9.1 Implement ReportController
    - `POST /api/reports` with { postId?, commentId?, reason, details? }
    - Validate target exists (404), duplicate check per user+target (409)
    - Create Report with status PENDING
    - Auto-flag: if post/comment now has 3+ reports from different users, set isFlagged=true
    - Add ReportStatus.RESOLVED to enum
    - _Requirements: 15.3, 15.4, 15.6_

  - [x] 9.2 Implement mod queue backend
    - `GET /api/mod/reports?page=0&size=20` — PENDING reports, ordered by createdAt DESC
    - `PATCH /api/mod/reports/{reportId}/dismiss` — set DISMISSED
    - `PATCH /api/mod/reports/{reportId}/remove` — set RESOLVED, set target isRemoved=true
    - `PATCH /api/mod/reports/{reportId}/ban` — set RESOLVED, set target isRemoved=true, set author isBanned=true
    - @PreAuthorize("hasAnyRole('ADMIN', 'MOD')")
    - _Requirements: 13.2, 13.3, 13.4, 13.5, 13.7_

  - [x] 9.3 Frontend — Report button UI
    - Add "Report" in ⋯ overflow menu on posts and comments
    - Only visible to authenticated users on content not authored by them
    - Modal: radio buttons for ReportReason + optional details (max 500 chars)
    - Confirmation message on success
    - _Requirements: 15.1, 15.2, 15.5, 15.7_

  - [x] 9.4 Frontend — Mod queue page
    - `/mod-queue` route, role-gate (redirect to / if not ADMIN/MOD)
    - Cards: post title, content preview, author, date, report reason, reporter, space
    - Dismiss/Remove/Ban buttons with optimistic UI + error rollback
    - _Requirements: 13.1, 13.3, 13.4, 13.5, 13.6, 13.7_

  - [ ]* 9.5 Write property tests for report system + mod queue
    - **Property 18: Auto-flag triggers at 3 reports from different users**
    - **Property 19: Mod queue access control (non-mod gets redirect)**
    - **Property 20: Duplicate report rejection**
    - **Validates: Requirements 13.1, 13.2, 15.4**

  - [x] 9.6 Report/mod UX improvements + share button
    - [x] 9.6.1 Fix report button — replace overflow menu with direct flag icon
      - ForumCard: replace ⋯ overflow menu with a direct flag icon (🚩) in actions row, same pattern as delete icon
      - Use `stopPropagation()` on click, only visible to authenticated non-author users
      - Keep overflow menu approach only on PostDetailPage and CommentItem (where there's more space)
      - _Requirements: 15.1_

    - [x] 9.6.2 Add ☰ menu button to desktop and mobile topbar
      - Desktop topbar: add ☰ button next to the notification bell, opens a dropdown menu
      - Mobile topbar: add ☰ button next to the bell icon, opens a slide-down/dropdown menu
      - Menu contents: "Settings" (placeholder link, disabled or → /settings 404 page for now)
      - For ADMIN/MOD users: "Moderation" link → `/mod-queue`, with flag/badge showing pending report count
      - Pending count: fetch via `GET /api/mod/reports/count` (new endpoint, returns `{ count: N }`)
      - Close menu on outside click or link click
      - _Requirements: 13.1 (mod queue discoverability)_

    - [x] 9.6.3 Backend: pending report count endpoint + mod notifications
      - Add `GET /api/mod/reports/count` — returns `{ count: N }` where N = number of PENDING reports. Protected with @PreAuthorize ADMIN/MOD
      - Add ReportRepository method: `long countByStatus(ReportStatus status)`
      - On report creation (in ReportService.createReport): create a Notification for all ADMIN/MOD users with type NEW_REPORT, message "New report on: {post title or comment excerpt}", link to `/mod-queue`
      - On mod remove/ban action (in ModQueueService): create a Notification for the content author with type CONTENT_REMOVED, message "Your {post/comment} was removed for violating community guidelines"
      - Add `NEW_REPORT` and `CONTENT_REMOVED` to NotificationType enum
      - _Requirements: new (mod notifications, author notification)_

    - [x] 9.6.4 Backend: auto-remove at 5 distinct reporters
      - In ReportService.createReport: after saving the report, if distinct reporter count reaches 5, set target `isRemoved=true` (in addition to the existing auto-flag at 3)
      - Notify the author when auto-removed (same CONTENT_REMOVED notification)
      - _Requirements: new (auto-remove threshold)_

    - [x] 9.6.5 Add share/copy-link button on posts
      - ForumCard: add a share icon (link/chain icon) in actions row
      - On click: copy `${window.location.origin}/post/${postId}` to clipboard via navigator.clipboard.writeText()
      - Show brief "Link copied!" toast/tooltip feedback (fade after 1.5s)
      - PostDetailPage: same share button in post actions area
      - Fallback for browsers without clipboard API: select+copy a hidden input
      - `stopPropagation()` on ForumCard to prevent card navigation
      - _Requirements: new (share/PWA)_

- [x] 10. Phase 2 — Backend + Frontend: Bookmarks
  - [x] 10.1 Create Bookmark entity and BookmarkController
    - Bookmark entity with unique constraint (user_id, post_id), Flyway migration
    - `POST /api/bookmarks/{postId}` — toggle (exists → delete, else → create)
    - `GET /api/bookmarks?page=0&size=20` — user's bookmarks, ordered by createdAt DESC, exclude deleted/removed/flagged
    - Add `isBookmarked` boolean to PostSummary DTO (populated for authenticated users)
    - _Requirements: 17.1, 17.2, 17.3, 17.5_

  - [x] 10.2 Frontend — Bookmark UI
    - Bookmark icon on ForumCard and PostDetailPage (outlined/filled toggle)
    - Optimistic UI on click
    - Unauthenticated → redirect to /login
    - "Saved" tab on profile page fetches GET /api/bookmarks
    - Empty state when no bookmarks
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7_

  - [x] 10.3 Write property tests for bookmarks
    - **Property 21: Bookmark toggle round trip**
    - **Property 22: Saved posts exclude deleted/removed/flagged**
    - **Validates: Requirements 17.1, 17.2, 17.3**

- [x] 11. Phase 2 — Frontend: Navigation restructuring + Explore page
  - [x] 11.1 Update routes and navigation
    - Rename ExplorePage.jsx → ExplorePage.jsx (keep name), route `/explore` stays at `/explore`
    - Delete ForumsPage.jsx, SearchPage.jsx
    - Add redirects: /forums → /explore, /search → /explore, /discover → /explore
    - Update profile route: add `/@:username` route pointing to ProfilePage
    - Add redirects: /users/:username → /@:username, /blog/:username → /@:username
    - Remove RequireAuth wrapper from main layout (public read access) — protect only write routes at component level
    - Update all internal navigation links (ForumCard @username → /@username, etc.)
    - _Requirements: 1.1, 5.2, 7.5_

  - [x] 11.2 Rewrite Explore page (`/explore`)
    - Search bar at top: on query, show tabbed results (Posts | Spaces | Users) via GET /api/search?q=&type=
    - Default state (no query):
      - Trending Topics section: horizontal scrollable tag pills from GET /api/trending-tags. Clicking a tag fills search with #tagName and shows filtered posts
      - Hot Discussions section: top 5 posts from GET /api/posts/hot, each showing title (2 lines), space name, like/comment counts
      - Popular Spaces section: grid of space cards from GET /api/spaces (sorted by member count)
    - URL supports `?q=` param (for desktop topbar search redirect)
    - _Requirements: 18.1, 18.5, 18.6, 18.7, 6.1, 6.2, 6.3, 6.4_

  - [x] 11.3 Rewrite BottomNav — 3 items
    - Home (/) | Explore (/explore) | Profile (/@username or /login)
    - Active state highlighted in gold (var(--accent))
    - Minimum 44x44px touch targets
    - _Requirements: 3.3, 3.5_

  - [x] 11.4 Update mobile topbar
    - Left: [+] create button (round gold, navigates to /create-post/discussion)
    - Center: Yap logo
    - Right: 🔔 notification bell with unread badge (navigates to /inbox on mobile)
    - Remove search icon from mobile topbar
    - _Requirements: 3.2, 3.4_

  - [x] 11.5 Update desktop left sidebar (Navbar.jsx)
    - EXPLORE_LINKS: Home (/) and Explore (/explore) only — remove Forums
    - Rename "+ Write" button to "+ New Discussion"
    - Keep: My Spaces list, Create Space button
    - _Requirements: 7.1, 7.3_

  - [x] 11.6 Update desktop right sidebar
    - Trending Topics: horizontal scrollable pills (clicking navigates to /explore?q=%23tagName)
    - Hot Discussions: top 3 posts (title truncated, space name, engagement counts)
    - Clicking post → /post/:postId, clicking space → /w/:spaceName
    - _Requirements: 6.1 (partial)_

  - [x] 11.7 Update desktop topbar
    - Add persistent search input (compact, right side)
    - On Enter: navigate to /explore?q={query}
    - Keep: notifications bell dropdown, user avatar menu
    - Fix user menu: "View profile" → navigate to /@username (not /blog/username)
    - _Requirements: 18.1_

- [x] 12. Phase 2 — Frontend: Profile page update
  - [x] 12.1 Update ProfilePage
    - Route: /@:username (strip @ in component to get username for API call)
    - Publicly viewable (no auth required to view)
    - Layout: large avatar, display name, @username, bio, stats row (posts · followers · following)
    - Edit Profile button (owner only, inline edit mode)
    - Gear icon (owner only) → settings (placeholder for now)
    - Remove banner image div
    - Three tabs: Posts | Liked | Saved
    - Saved tab fetches GET /api/bookmarks (only visible content for owner)
    - _Requirements: 7.6, 17.3, 17.4_

- [x] 13. Phase 2 — Frontend: Post flair UI + avatar display
  - [x] 13.1 Flair selector in CreateDiscussionPage
    - Replace tag input with flair picker: horizontal row of pill buttons (DISCUSSION, SUPPORT, RANT, RESOURCE, QUESTION, SENSITIVE)
    - One selectable at a time, optional (can post without flair)
    - Color coding: Discussion=gold, Support=teal(#1D9E75), Rant=red(#C0392B), Resource=blue(#2E86C1), Question=purple(#7D3C98), Sensitive=orange(#D35400)
    - Remove tag input from create form
    - _Requirements: new (post flair)_

  - [x] 13.2 Show flair on ForumCard + PostDetailPage
    - Colored pill with flair name (if post has flair set)
    - SENSITIVE flair shows small warning icon
    - Remove #tag pill rendering everywhere
    - _Requirements: new (post flair)_

  - [x] 13.3 Show author avatars
    - ForumCard: 24px avatar circle next to @username
    - CommentItem: 24px avatar next to @username
    - Use Avatar component with authorAvatarUrl from PostSummary DTO
    - Avatar component falls back to generated initials (deterministic color + initials from username) when no URL is set
    - _Requirements: new (avatar display)_

- [x] 14. Phase 2 — Frontend: Notifications/Inbox page
  - [x] 14.1 Create InboxPage (/inbox)
    - Fetch GET /api/notifications
    - Display: avatar, message text, timestamp, link to relevant post/comment
    - Mark all read on page open via POST /api/notifications/mark-all-read
    - Empty state: "You're all caught up ✓"
    - Pagination: load more button
    - _Requirements: new (inbox page)_

  - [x] 14.2 Bell icon behavior
    - Desktop: clicking opens dropdown panel (max 10 items, "View all" → /inbox)
    - Mobile: bell navigates directly to /inbox
    - Unread count badge (red dot or number)
    - _Requirements: new (notifications)_

- [x] 15. Phase 2 — Frontend: Space selector on create discussion
  - [x] 15.1 Space selector on create discussion
    - If ?space= query param present: pre-select that space, show space guidelines
    - Otherwise: show dropdown of ALL spaces (with search/filter)
    - Fetch space guidelines from community.rules field, show below title (desktop: visible, mobile: collapsed toggle)
    - _Requirements: new (space selector)_

- [x] 16. Phase 2 — Frontend: PWA install nudge
  - [x] 16.1 Implement install prompt
    - Detect installability via `beforeinstallprompt` event
    - Show dismissible banner once per session to logged-in mobile users who haven't installed
    - For iOS: detect Safari and show manual instructions ("Share → Add to Home Screen")
    - Store dismissal in localStorage
    - Fix PWA colors: theme_color #C4973F, background_color #13151F, apple-mobile-web-app-status-bar-style: black-translucent
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

- [x] 17. Checkpoint — Phase 2 complete
  - Ensure all tests pass, verify: rate limiting (429 on exceeding limits), report → auto-flag → mod queue, bookmarks toggle, Explore page sections, mobile nav, desktop search, profile at /@username, PWA install nudge.

- [ ] 18. Phase 3 — Frontend: GIF picker (posts + comments)
  - [ ] 18.1 Implement GifPicker component
    - Toggle button in post creation form AND comment composer
    - Initial state: Giphy trending (/v1/gifs/trending?limit=25)
    - Search: 500ms debounce → Giphy search, 25 results in grid
    - On select: close picker, show animated preview, store URL in gifUrl field
    - Only 1 GIF per post/comment, selecting replaces previous
    - Remove button clears gifUrl
    - API key via VITE_GIPHY_API_KEY
    - Display in posts/comments: max-width 400px, preserve aspect ratio
    - Error handling: inline message on API failure
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8_

  - [ ] 18.2 Implement EmojiPicker and shared ComposerToolbar
    - Install `emoji-picker-react` (lightweight, ~50KB gzipped, accessible, searchable)
    - Create `<EmojiPicker>` wrapper component with popover positioning (opens above/below button)
    - Create `<ComposerToolbar>` component containing emoji button + GIF button
    - Integrate ComposerToolbar into CreateDiscussionPage textarea
    - Integrate ComposerToolbar into comment composer in PostDetailPage
    - Cursor-position-aware insertion: insert emoji at `selectionStart`, not append to end
    - Use `requestAnimationFrame` to restore cursor position after React re-render
    - Picker closes on outside click and on emoji selection
    - On mobile (≤768px): hide emoji button (native keyboard handles it), show GIF button only
    - Detect mobile via `window.matchMedia('(max-width: 768px)')` or CSS class
    - _Requirements: new (emoji picker)_

- [ ] 19. Phase 3 — Backend + Frontend: Search controller
  - [ ] 19.1 Implement SearchController
    - `GET /api/search?q={query}&type={posts|spaces|users}&page=0&size=20`
    - Validation: min 2 chars, max 100 chars
    - Posts: ILIKE on title + content_text, exclude deleted/removed/flagged, order by created_at DESC
    - Spaces: ILIKE on name + description, order by member_count DESC
    - Users: ILIKE on username, exclude banned/inactive
    - _Requirements: 18.2, 18.3, 18.4_

  - [ ]* 19.2 Write property tests for search
    - **Property 23: Search results match query (case-insensitive)**
    - **Property 24: Search excludes banned/removed content**
    - **Validates: Requirements 18.2, 18.3, 18.4**

- [ ] 20. Phase 3 — Backend + Frontend: Email verification and password reset
  - [ ] 20.1 Add email verification fields to User entity and implement AuthService verification flow
    - Add fields to User: `emailVerified`, `verificationToken`, `verificationTokenExpiry`
    - On register: generate 32+ char token via SecureRandom, set 24hr expiry
    - Send verification email via Resend REST API (RestClient POST to `https://api.resend.com/emails`)
    - If Resend fails: log error, registration still succeeds
    - Implement `POST /api/auth/verify-email?token={token}` — validate token exists, not expired, not used → set emailVerified=true, nullify token
    - Invalid/expired token → 400 with error message
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ] 20.2 Implement resend verification endpoint
    - `POST /api/auth/resend-verification` — generates new token (invalidates old), sends email
    - Rate limit: 3 resend requests per user per hour (DB count check on token generation timestamps)
    - _Requirements: 9.6_

  - [ ] 20.3 Implement password reset flow
    - Add fields to User: `resetToken`, `resetTokenExpiry`, `passwordChangedAt`
    - `POST /api/auth/forgot-password` — always returns 200 (no email enumeration)
    - If email exists: generate reset token (32+ chars, 1hr expiry), send link via Resend
    - `POST /api/auth/reset-password` — validate token, enforce min 8 chars, BCrypt hash, update password
    - Set `passwordChangedAt = now()`, nullify reset token
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.6_

  - [ ] 20.4 Implement JWT session invalidation on password change
    - In JwtAuthFilter: compare JWT `iat` claim against user's `passwordChangedAt`
    - Reject tokens issued before password change timestamp
    - _Requirements: 10.5_

  - [ ] 20.5 Frontend — Verification reminder banner
    - Dismissible banner at top when user's email is unverified
    - "Resend verification" link calls POST /api/auth/resend-verification
    - Success/error feedback
    - _Requirements: 9.7_

  - [ ]* 20.6 Write property tests for email verification and password reset
    - **Property 6: Verification token single-use round trip**
    - **Property 7: Password reset does not reveal email registration status**
    - **Property 8: Password reset invalidates prior sessions**
    - **Property 9: Password policy enforcement on reset**
    - **Validates: Requirements 9.3, 9.4, 10.1, 10.5, 10.6**

- [ ] 21. Phase 3 — Deployment preparation
  - [ ] 21.1 Backend (Render)
    - Verify env vars in Render dashboard: DATABASE_URL (Neon PostgreSQL connection string), JWT_SECRET (min 32 chars), FRONTEND_URL (production Vercel URL), RESEND_API_KEY, CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
    - Verify DATABASE_URL has no `characterEncoding` override — Neon PostgreSQL handles UTF-8/emoji natively. If emojis show as `?`, add `?options=--client_encoding=UTF8` to connection string
    - Remove spring-boot-starter-webflux from pom.xml (if present)
    - Confirm actuator + bucket4j-core present
    - Set FRONTEND_URL to production Vercel URL
    - _Requirements: 4.3, 4.4_

  - [ ] 21.2 Frontend (Vercel)
    - Set env vars: VITE_API_URL, VITE_CLOUDINARY_CLOUD_NAME, VITE_GIPHY_API_KEY
    - Confirm PWA icons exist (192, 512, apple-touch-icon 180)
    - _Requirements: 8.1, 8.6_

  - [ ] 21.3 Database + Flyway
    - Verify all migrations run cleanly: flair column, bookmarks table, passwordChangedAt, email verification fields, reset token fields
    - Seed 3 spaces with starter posts
    - _Requirements: new (deployment)_

  - [ ] 21.4 External services
    - Configure cron-job.org: GET /actuator/health every 14 min, 30s timeout, 1 retry
    - _Requirements: 4.1, 4.2_

- [ ] 22. Final checkpoint — all tests pass, pre-launch checklist
  - Test flows: register → verify email, password reset, report → mod queue, rate limiting (429 on 6th attempt), public browsing without auth, PWA install on iOS/Android, mobile layout at 375px
  - Verify all routes resolve correctly, redirects work, no broken links
  - Confirm Explore page loads all sections, search works end-to-end
  - Verify flair display, bookmark toggle, avatar rendering

## Notes

- Tasks marked with `*` are optional property tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation after each phase
- Property tests validate universal correctness properties (jqwik for Java backend, fast-check for frontend)
- The keep-alive cron job (Requirement 4.1, 4.2) is configured externally on cron-job.org — no code task needed beyond exposing the health endpoint (task 1.2)
- Backend retains `PostType.BLOG` enum and schema (Requirement 7.4) — no backend changes needed for blog removal
- Icon asset creation (192px, 512px, 180px apple-touch-icon) may require design input from the user
- Post flair replaces tag UI in the frontend — tags remain in DB but are hidden from user-facing views
- No member gate for posting in v1 — anyone can post in any space
- Route changes: /forums → removed, /search → removed, /discover → /explore, /users/:username → /@:username, /blog/:username → /@:username
- GIF picker uses Giphy API (free tier) with VITE_GIPHY_API_KEY env var
- Emoji picker uses emoji-picker-react (desktop only — mobile uses native keyboard emoji). GIF + emoji buttons share a ComposerToolbar component
- Feed algorithm uses 30-day window (configurable), not 7-day as in original spec — decided during design phase
- Cold start behavior: users with <3 joined spaces get 70% trending + 30% personalized blend
- Report UX: ForumCard uses direct flag icon (no overflow menu); PostDetailPage keeps overflow menu pattern
- ☰ menu in topbar provides access to Settings and Moderation (for mods), replaces the need for sidebar mod link
- Auto-remove at 5 distinct reporters supplements auto-flag at 3 — community-driven moderation
- Share button copies post URL to clipboard for easy PWA sharing

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["7.1", "8.1"] },
    { "id": 1, "tasks": ["7.2", "8.2", "8.3"] },
    { "id": 2, "tasks": ["7.3*", "9.1", "10.1"] },
    { "id": 3, "tasks": ["9.2", "9.3", "10.2", "11.1"] },
    { "id": 4, "tasks": ["9.4", "9.5*", "9.6.3", "10.3*", "11.2", "11.3"] },
    { "id": 5, "tasks": ["9.6.1", "9.6.2", "9.6.4", "9.6.5", "11.4", "11.5", "11.6", "11.7", "12.1"] },
    { "id": 6, "tasks": ["13.1", "13.2", "13.3", "14.1", "14.2"] },
    { "id": 7, "tasks": ["15.1", "16.1"] },
    { "id": 8, "tasks": ["18.1", "18.2", "19.1"] },
    { "id": 9, "tasks": ["19.2*", "20.1"] },
    { "id": 10, "tasks": ["20.2", "20.3"] },
    { "id": 11, "tasks": ["20.4", "20.5"] },
    { "id": 12, "tasks": ["20.6*", "21.1", "21.2", "21.3", "21.4"] },
    { "id": 13, "tasks": ["22"] }
  ]
}
```
