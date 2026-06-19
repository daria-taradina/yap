# Requirements Document

## Introduction

Yap is a community-first, safety-focused discussion platform built by women for women (but inclusive of everyone). The core differentiator is a moderation philosophy that does NOT over-censor important conversations (domestic violence, sexual assault, women's health — mental and physical, including sensitive topics) like other platforms do, while actively filtering toxic/offensive content, bullying, and bot behavior through a report-based moderation system: users report harmful content, which enters a manual moderation queue reviewed by the founder/moderators. Reddit serves as structural inspiration (layout/navigation), but Yap aims to be a better, safer version that women want to use.

The target audience is women, reached primarily through TikTok and Instagram. The platform operates on a $0 budget using all free tiers (Vercel, Render, Resend, Giphy, cron-job.org). It is web-first with PWA capabilities for mobile home screen installation — no native mobile app is planned for the foreseeable future.

Security is the top priority: the platform must be prepared for hostile actors (bot floods, toxic raids, DDoS-style attacks) attempting to bring it down once it gains attention.

This document captures the v1 launch requirements across three priority phases: pre-launch blockers that must ship before any public link is shared, features that make the platform feel production-ready before active promotion, and growth/retention features for after the first real users arrive. The backend is Java Spring Boot with PostgreSQL; the frontend is React deployed on Vercel; the backend is hosted on Render's free tier.

## Glossary

- **Platform**: The Yap web application comprising the React frontend and Spring Boot backend
- **Feed_Service**: The backend service responsible for assembling and returning post feeds
- **Security_Config**: The Spring Security configuration controlling authentication and CORS rules
- **Auth_Service**: The backend service handling user registration, login, email verification, and password reset
- **Rate_Limiter**: The Bucket4j-based middleware limiting request rates on auth endpoints
- **Tag_Service**: The backend service managing tag creation, trending tag computation, and tag-based post retrieval
- **Feed_Algorithm**: The logic that selects and ranks posts for a user's feed
- **Mod_Queue**: The administrative interface showing reported posts for moderator review
- **Report_Controller**: The backend controller wiring the existing Report entity to REST endpoints
- **Frontend**: The React single-page application served from Vercel
- **Keep_Alive_Job**: An external cron job that pings the backend health endpoint to prevent cold starts
- **Space**: A community (also called "w/spaceName") where users can post discussions
- **ForumCard**: The React component rendering a post summary in feed and space views
- **Bookmark_Service**: The backend service managing saved/bookmarked posts for users
- **Search_Service**: The backend service handling keyword search across posts, spaces, and users
- **Service_Worker**: The client-side script enabling offline caching and PWA functionality

## Requirements

---

### Phase 1: Pre-Launch Blockers

---

### Requirement 1: Public Read Access

**User Story:** As a visitor, I want to browse the feed, posts, spaces, and profiles without logging in, so that I can evaluate Yap before creating an account.

#### Acceptance Criteria

1. WHEN an unauthenticated request is made to a GET endpoint for feed, posts, spaces, or profiles, THE Security_Config SHALL permit the request without requiring a JWT token, excluding user-specific endpoints that depend on the authenticated principal (such as `/api/users/me` and `/api/spaces/my`)
2. THE Security_Config SHALL require authentication for all POST, PUT, PATCH, and DELETE endpoints except `/api/auth/**`
3. THE Security_Config SHALL include the production Vercel URL and `http://localhost:5173` in the CORS allowed-origins list, and SHALL reject cross-origin requests from origins not in that list by omitting CORS response headers
4. WHEN an unauthenticated request is made to a non-GET endpoint (excluding `/api/auth/**`), THE Security_Config SHALL respond with HTTP 401 Unauthorized and a JSON response body containing an error message indicating authentication is required
5. WHEN an unauthenticated request is made to a user-specific GET endpoint that requires the authenticated principal, THE Security_Config SHALL respond with HTTP 401 Unauthorized

---

### Requirement 2: New Discussion Placement and Delete Button

**User Story:** As a user browsing a space, I want the "New Discussion" action to be prominent and I want to delete my own posts, so that I can easily contribute and manage my content.

#### Acceptance Criteria

1. THE Frontend SHALL render the "New Discussion" button inline with the space header on the SpacePage component
2. IF an unauthenticated user clicks the "New Discussion" button, THEN THE Frontend SHALL redirect the user to the login page
3. WHEN a post's `canDelete` flag is true, THE Frontend SHALL render a delete button on that post
4. WHEN the user clicks the delete button, THE Frontend SHALL display a confirmation dialog asking the user to confirm the deletion before proceeding
5. WHEN the user confirms the deletion, THE Frontend SHALL call the DELETE `/api/posts/{postId}` endpoint and remove the post from the view upon receiving a successful response
6. IF the delete request fails, THEN THE Frontend SHALL display an error message indicating that the post could not be deleted and SHALL keep the post visible in the view

---

### Requirement 3: Mobile-Responsive Layout

**User Story:** As a mobile user arriving from TikTok, I want Yap to be fully usable on my phone, so that I can browse and engage without a desktop.

#### Acceptance Criteria

1. THE Frontend SHALL include a viewport meta tag with `width=device-width, initial-scale=1` in the HTML document head
2. WHILE the viewport width is 768px or less, THE Frontend SHALL hide the left and right sidebars and display a single-column layout for feed content
3. WHILE the viewport width is 768px or less, THE Frontend SHALL render a bottom navigation bar with icons for Home, Search, Create, and Profile
4. WHILE the viewport width is 768px or less, THE Frontend SHALL hide the desktop top navigation bar
5. THE Frontend SHALL ensure all interactive elements (buttons, links, inputs) have a minimum touch target size of 44x44 CSS pixels on viewports 768px or less
6. THE Frontend SHALL set `font-size: 16px` minimum on all `<input>`, `<textarea>`, and `<select>` elements to prevent iOS auto-zoom on focus
7. THE Frontend SHALL prevent horizontal overflow on viewports 768px or less so that no horizontal scrollbar appears

---

### Requirement 4: Render Cold Start Keep-Alive

**User Story:** As a platform operator, I want the backend kept warm on Render's free tier, so that the first visitor does not experience a 30-second cold start.

#### Acceptance Criteria

1. THE Keep_Alive_Job SHALL send an HTTP GET request to the `/actuator/health` endpoint every 14 minutes, with a request timeout of 30 seconds
2. IF the health endpoint responds with a non-2xx status or the request times out, THEN THE Keep_Alive_Job SHALL retry the request once after 30 seconds
3. THE Platform SHALL expose the `/actuator/health` endpoint without authentication, and it SHALL respond with HTTP 200 and a JSON body containing a `status` field when the application is running
4. WHEN the `/actuator/health` endpoint receives a request, THE Platform SHALL respond within 5 seconds under normal (warm) operating conditions

---

### Requirement 5: Clickable Navigation for Spaces, Usernames, and Tags

**User Story:** As a user viewing a post, I want space names, usernames, and tags to be clickable links, so that I can navigate to related content.

#### Acceptance Criteria

1. WHEN a user clicks a `w/spaceName` link on a ForumCard, THE Frontend SHALL navigate to the `/w/{spaceName}` route without also triggering the ForumCard's post-detail navigation
2. WHEN a user clicks a `@username` link on a ForumCard, THE Frontend SHALL navigate to the `/users/{username}` route without also triggering the ForumCard's post-detail navigation
3. WHEN a user clicks a tag on a ForumCard, THE Frontend SHALL navigate to the feed page with a `tag` query parameter set to that tag's name
4. THE Frontend SHALL render `w/spaceName`, `@username`, and tag elements as anchor elements (links) with a visually differentiated style from surrounding text (distinct color and underline or pointer cursor on hover)
5. THE Frontend SHALL ensure that `w/spaceName`, `@username`, and tag links are keyboard-focusable and activatable via the Enter key
6. IF a `w/spaceName`, `@username`, or tag link points to a resource that does not exist, THEN THE Frontend SHALL display the target page's standard empty state or not-found indicator

---

### Requirement 6: Wire Trending Tags to Feed

**User Story:** As a user, I want clicking a trending tag to show me posts with that tag, so that I can explore content by topic.

#### Acceptance Criteria

1. WHEN the FeedPage URL contains a `tag` query parameter with a non-empty value, THE Frontend SHALL call the `/posts/tag/{tagName}` endpoint using the parameter value instead of the default `getFeed` endpoint, and display the returned posts
2. WHEN a tag filter is active, THE Frontend SHALL display the tag name as a visible label alongside a clear button that, when activated, removes the `tag` query parameter from the URL
3. WHEN the `tag` query parameter is removed from the URL or is empty, THE Frontend SHALL revert to calling the default `getFeed` endpoint and display the default feed without any tag filter label
4. IF the `getPostsByTag` endpoint returns an empty list for the active tag, THEN THE Frontend SHALL display an empty-state message indicating no posts were found for that tag, while keeping the active tag filter label and clear button visible

---

### Requirement 7: Remove Blog Features from V1 UI

**User Story:** As a platform operator, I want blog-related UI removed from v1, so that the product focus remains on discussions and the interface is not cluttered with unused features.

#### Acceptance Criteria

1. THE Frontend SHALL remove the "Blogs" navigation item from the sidebar navigation link list
2. THE Frontend SHALL remove the routes for `/blogs`, `/create-post/blog`, and `/blog/:username` from the router configuration
3. WHEN a user activates the "Write" button, THE Frontend SHALL navigate directly to the create-discussion page without displaying a post-type picker
4. THE Backend SHALL retain the PostType.BLOG enum value, the postType column in the posts table, and existing blog-related repository query methods unchanged
5. WHEN a user navigates directly to `/blogs`, `/create-post/blog`, or `/blog/:username`, THE Frontend SHALL redirect to the home page (`/`)
6. THE Frontend SHALL remove the "Blog Posts" tab from the profile page and display only discussion posts and liked posts as profile content tabs

---

### Requirement 8: Progressive Web App (PWA) Support

**User Story:** As a mobile user, I want to add Yap to my home screen and get an app-like experience, so that I can access it quickly without opening a browser.

#### Acceptance Criteria

1. THE Frontend SHALL serve a web app manifest containing: application name set to "Yap", a `start_url` of `/`, `display` set to `standalone`, a defined `theme_color`, a defined `background_color`, and an icons array including at least a 192x192 pixel icon and a 512x512 pixel icon in PNG format
2. THE Frontend SHALL register a service worker using vite-plugin-pwa that pre-caches the application shell (index.html, CSS bundles, JS bundles) at service worker install time
3. WHEN a user opens Yap from the home screen icon, THE Frontend SHALL display in standalone mode without browser navigation chrome
4. THE Frontend SHALL include iOS-specific meta tags in the HTML document head: `apple-mobile-web-app-capable` set to `yes`, an `apple-touch-icon` link referencing a 180x180 pixel icon, and `apple-mobile-web-app-status-bar-style`
5. WHEN the device has no network connectivity and the user navigates to any route, THE Frontend SHALL display a pre-cached offline fallback page indicating that the device is not connected and content will be available when connectivity is restored
6. THE Frontend SHALL pass the Lighthouse PWA installability check (valid manifest with required fields, registered service worker, icons meeting minimum size requirements)

---

### Phase 2: Makes It Feel Real

---

### Requirement 9: Email Verification

**User Story:** As a platform operator, I want users to verify their email address after registration, so that bot signups are reduced and accounts have valid contact information.

#### Acceptance Criteria

1. WHEN a user registers, THE Auth_Service SHALL send a verification email to the provided address using the Resend API containing a verification link with the token as a query parameter
2. WHEN a user registers, THE Auth_Service SHALL generate a cryptographically random verification token of at least 32 characters, valid for 24 hours and usable only once
3. WHEN a user submits a valid, unexpired, unused verification token, THE Auth_Service SHALL mark the user's email as verified and invalidate the token
4. IF the verification token is expired, already used, or not found, THEN THE Auth_Service SHALL respond with HTTP 400 and an error message indicating the token is invalid or expired
5. IF the Resend API call fails or returns an error, THEN THE Auth_Service SHALL complete the registration without sending the email and allow the user to request a new verification email later
6. WHEN an authenticated user with an unverified email requests a new verification email, THE Auth_Service SHALL generate a new token (invalidating any prior token) and send a new verification email, limited to 3 resend requests per user per hour
7. WHILE a user's email is unverified, THE Auth_Service SHALL allow login and THE Frontend SHALL display a dismissible verification reminder banner at the top of the page on every page load

---

### Requirement 10: Password Reset

**User Story:** As a user who forgot my password, I want to reset it via email, so that I can regain access to my account.

#### Acceptance Criteria

1. WHEN a user submits a password reset request with an email address, THE Auth_Service SHALL respond with a success confirmation regardless of whether the email is registered, and SHALL only send a reset link via the Resend API if the email matches an existing account
2. WHEN a password reset is requested for a registered email, THE Auth_Service SHALL generate a cryptographically random, single-use reset token valid for 1 hour
3. WHEN a valid reset token is submitted with a new password that meets the password policy (minimum 8 characters), THE Auth_Service SHALL update the user's password hash and immediately invalidate the used token
4. IF the reset token is expired, already used, or does not exist, THEN THE Auth_Service SHALL respond with HTTP 400 and an error message indicating the token is invalid without revealing the specific reason
5. WHEN a password reset is completed, THE Auth_Service SHALL invalidate all existing sessions for that user so that previously issued JWT tokens are no longer accepted
6. IF the new password submitted during reset does not meet the password policy, THEN THE Auth_Service SHALL respond with HTTP 400 and an error message indicating the password requirements

---

### Requirement 11: Rate Limiting on Auth Endpoints

**User Story:** As a platform operator, I want login and registration endpoints rate-limited, so that brute-force and credential-stuffing attacks are mitigated.

#### Acceptance Criteria

1. THE Rate_Limiter SHALL allow a maximum of 5 requests per IP address per minute to the `/api/auth/login` endpoint, tracked independently from other endpoints
2. THE Rate_Limiter SHALL allow a maximum of 5 requests per IP address per minute to the `/api/auth/register` endpoint, tracked independently from other endpoints
3. WHEN a client exceeds the rate limit, THE Rate_Limiter SHALL respond with HTTP 429 Too Many Requests, a `Retry-After` header indicating the number of seconds until the next token is available, and a JSON body with an error message
4. THE Rate_Limiter SHALL resolve client IP from the `X-Forwarded-For` header (first value) to correctly identify clients behind Render's reverse proxy, falling back to the remote address if the header is absent
5. THE Rate_Limiter SHALL use in-memory token bucket storage via Bucket4j with no external dependencies, evicting entries that have been inactive for more than 10 minutes to prevent memory exhaustion during large-scale attacks
6. THE Rate_Limiter SHALL not rate-limit any endpoints other than `/api/auth/login` and `/api/auth/register`

---

### Requirement 12: Tag Input UX with Pills

**User Story:** As a user creating a post, I want to add tags as visual pills by pressing Enter or comma, so that tagging is intuitive and clear.

#### Acceptance Criteria

1. WHEN the user presses Enter or types a comma in the tag input, THE Frontend SHALL trim the text, convert it to lowercase, and if the result is non-empty and contains only alphanumeric characters, convert it into a visual pill/chip element
2. THE Frontend SHALL allow a maximum of 5 tags per post, matching the backend PostCreate DTO validation constraint
3. WHEN the user attempts to add a 6th tag, THE Frontend SHALL prevent addition and display an inline message indicating the maximum of 5 tags has been reached
4. THE Frontend SHALL prevent duplicate tags by comparing the lowercased input against existing pill values and rejecting matches silently
5. THE Frontend SHALL enforce a maximum of 20 characters per tag, matching the backend Tag entity column length constraint
6. WHEN the user clicks the remove icon on a tag pill, THE Frontend SHALL remove that tag from the list and re-enable the input if it was disabled at the 5-tag maximum

---

### Requirement 13: Mod Queue Page

**User Story:** As a moderator, I want a dedicated page showing reported posts, so that I can review and take action on content that users have flagged.

#### Acceptance Criteria

1. THE Frontend SHALL provide a `/mod-queue` route accessible only to users with the ADMIN or MOD role
2. THE Mod_Queue SHALL display all posts that have associated Report entities with status PENDING, ordered by report creation date descending, paginated with a default page size of 20 posts
3. THE Mod_Queue SHALL display for each reported post the post title, content text, author username, creation date, report reason, reporter username, and the space it was posted in
4. WHEN a moderator clicks "Dismiss", THE Mod_Queue SHALL update the report status to DISMISSED and remove the post from the queue list without a full page reload
5. WHEN a moderator clicks "Remove", THE Mod_Queue SHALL set the post's `isRemoved` field to true, update the report status to RESOLVED, and remove the post from the queue list without a full page reload
6. IF a "Dismiss" or "Remove" action fails, THEN THE Mod_Queue SHALL display an error message indicating the action could not be completed and keep the post in the queue at its current position
7. IF a user without the ADMIN or MOD role navigates to `/mod-queue`, THEN THE Frontend SHALL redirect to the home page

---

### Phase 3: Growth & Retention

---

### Requirement 14: Smarter Feed Algorithm

**User Story:** As a user, I want a feed that surfaces fresh, high-engagement content and introduces me to new spaces, so that I stay engaged with the platform.

#### Acceptance Criteria

1. THE Feed_Algorithm SHALL apply a time-decay factor that halves a post's ranking score for every 24 hours elapsed since the post's `createdAt` timestamp
2. THE Feed_Algorithm SHALL only consider posts created within the last 7 days for feed ranking
3. WHEN a post receives 5 or more likes within its most recent 60-minute window, THE Feed_Algorithm SHALL apply a velocity boost that doubles the post's ranking score
4. THE Feed_Algorithm SHALL inject 1 to 2 posts from spaces the user does not follow into each feed page of 20 posts, selecting discovery posts from spaces with the highest member count that the user has not yet seen in the current session
5. THE Feed_Algorithm SHALL exclude posts where `isFlagged` is true, `isRemoved` is true, or `isDeleted` is true from all feed responses
6. WHEN an unauthenticated user requests the feed, THE Feed_Algorithm SHALL return posts ranked solely by like count within the last 7 days without personalization or discovery injection
7. IF fewer than 2 eligible discovery posts exist from non-followed spaces, THEN THE Feed_Algorithm SHALL fill remaining feed slots with additional ranked posts from followed spaces

---

### Requirement 15: Report Button on Posts and Comments

**User Story:** As a user, I want to report posts or comments that violate community guidelines, so that moderators can review them.

#### Acceptance Criteria

1. WHILE a user is authenticated, THE Frontend SHALL render a "Report" button on each post and comment that was not authored by that user
2. WHEN a user clicks "Report", THE Frontend SHALL display a form requiring selection of exactly one ReportReason (HARASSMENT, HATE_SPEECH, SPAM, MISINFORMATION, SELF_HARM, or OTHER) and an optional details text field accepting at most 500 characters
3. WHEN the report form is submitted with a valid reason and an existing target post or comment, THE Report_Controller SHALL create a Report entity with the authenticated user as reporter, the selected target, the chosen reason, the optional details, and status PENDING
4. IF the authenticated user has already submitted a report for the same post or comment (regardless of reason), THEN THE Report_Controller SHALL reject the submission and return an error response indicating that a duplicate report exists
5. IF the report form is submitted without a reason selected, THEN THE Frontend SHALL display a validation error indicating that a reason is required and SHALL NOT submit the request
6. IF the report targets a post or comment that does not exist, THEN THE Report_Controller SHALL return an error response indicating the target was not found
7. WHEN a report is successfully created, THE Frontend SHALL display a confirmation message indicating the report has been received

---

### Requirement 16: GIF Picker in Post Creation

**User Story:** As a user creating a post, I want to search for and attach a GIF, so that my discussions are more expressive and engaging.

#### Acceptance Criteria

1. THE Frontend SHALL render a GIF picker button in the post creation form
2. WHEN the user clicks the GIF picker button, THE Frontend SHALL display a search interface that shows trending GIFs from the Giphy API as the initial state and a text input for searching
3. WHEN the user types in the GIF search input, THE Frontend SHALL query the Giphy Search API after a 500ms debounce delay and display up to 25 results in a scrollable grid
4. WHEN the user selects a GIF from the search results, THE Frontend SHALL close the picker, display an animated preview of the selected GIF in the post creation form, and store the Giphy CDN URL (matching the domain `media*.giphy.com`) in the `gifUrl` field
5. THE Frontend SHALL allow only one GIF per post; selecting a new GIF SHALL replace the previously selected one
6. WHEN the user clicks the remove button on the GIF preview, THE Frontend SHALL remove the preview and clear the `gifUrl` field
7. WHEN a post with a non-null `gifUrl` is displayed, THE Frontend SHALL render the GIF as an animated image inline within the post content with a maximum display width of 400px, preserving the original aspect ratio
8. IF the Giphy API request fails or returns no results, THEN THE Frontend SHALL display an inline message indicating that GIFs are temporarily unavailable or that no results matched the query, respectively

---

### Requirement 17: Bookmarks / Saved Posts

**User Story:** As a user, I want to bookmark posts for later reading, so that I can easily find discussions I found interesting or want to come back to.

#### Acceptance Criteria

1. WHEN an authenticated user clicks the bookmark button on a post that is not already bookmarked, THE Bookmark_Service SHALL create a bookmark record with a unique constraint on (user_id, post_id) and THE Frontend SHALL switch the bookmark icon to its filled state
2. WHEN an authenticated user clicks the bookmark button on an already-bookmarked post, THE Bookmark_Service SHALL delete the bookmark record and THE Frontend SHALL switch the bookmark icon to its outlined (unfilled) state
3. THE Frontend SHALL render a `/saved` page that displays the authenticated user's bookmarked posts in reverse chronological order of bookmark creation time, paginated with a default page size of 20, excluding posts that have been deleted, removed, or flagged
4. WHEN the authenticated user has no bookmarked posts, THE Frontend SHALL display an empty state message indicating no saved posts exist
5. THE Frontend SHALL render the bookmark icon in a filled state on posts the current authenticated user has bookmarked and in an outlined state on posts the user has not bookmarked
6. IF an unauthenticated user clicks the bookmark button, THEN THE Frontend SHALL redirect the user to the login page
7. IF an unauthenticated user navigates to the `/saved` page, THEN THE Frontend SHALL redirect the user to the login page

---

### Requirement 18: Search

**User Story:** As a user, I want to search for posts, spaces, and users by keyword, so that I can find specific content or communities.

#### Acceptance Criteria

1. THE Frontend SHALL render a search input in the top navigation bar
2. WHEN a user submits a search query of at least 2 characters, THE Search_Service SHALL return matching posts (by title and content), spaces (by name and description), and users (by username) using PostgreSQL ILIKE substring matching, paginated with a default page size of 20 results per category
3. THE Search_Service SHALL perform case-insensitive substring matching on search queries
4. THE Search_Service SHALL exclude posts where `isDeleted`, `isRemoved`, or `isFlagged` is true, and SHALL exclude users where `isBanned` is true or `isActive` is false, from all search results
5. THE Frontend SHALL display search results grouped by category tabs (Posts, Spaces, Users) with the ability to select a tab to show only that category's results
6. WHEN a search query returns no results for the active category, THE Frontend SHALL display an empty state message indicating no matches were found
7. IF a user submits a search query shorter than 2 characters, THEN THE Frontend SHALL display an inline validation message indicating the minimum query length and SHALL NOT send a request to the backend
