# Yap 🐱✦
### *A forum for women who have opinions.*

> Reddit structure. Safe space culture. Built by women, for everyone.

---

## What is Yap?

Yap is a community forum platform where safety is a design constraint, not an afterthought. Women (and everyone else) can create spaces (`w/communities`), start discussions, write blog posts, and actually feel comfortable doing it.

**The problem:** Reddit was built in 2005 by men, for men. Women are 40% of internet users and roughly 35% of Reddit — but the culture, defaults, and moderation philosophy reflect who built it. Substack is lonely. Discord is fragmented. There's no middle ground between "broadcast to nobody" and "get ratio'd into silence."

**The solution:** A forum with no downvotes, multi-layer moderation, and a community-first brand that doesn't feel like a utility.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.0.6, Java 25 |
| Database | PostgreSQL (Neon — serverless) |
| Auth | JWT (jjwt 0.12.6) |
| Frontend | React 18 + Vite |
| Deployment | Render (backend) + Vercel (frontend) |
| Media | Cloudinary |
| Moderation | Perspective API (Google) |

---

## Features

### Core
- `w/community` forum spaces with categories, descriptions, and guidelines
- Discussion posts and blog posts
- Nested comments (one level of replies)
- Upvotes on posts and comments — no downvotes
- Tags on posts, trending tags in sidebar
- Personalized feed (communities + follows) with hot-score ranking

### Safety & Moderation
- Community reporting system (`HARASSMENT`, `HATE_SPEECH`, `SPAM`, `MISINFORMATION`, `SELF_HARM`, `OTHER`)
- Perspective API toxicity scoring on every post and comment at creation
- Auto-flagging for content scoring above 0.85 toxicity threshold
- Community mod system — owners can promote trusted members
- Soft delete (author) vs hard remove (mod) — tracked separately

### Social
- Follow users
- Notifications (likes, comments, replies, follows, mod actions)
- User profiles with post history, community posts, liked posts

---

## Project Structure

```
yap/
├── yap-backend/          # Spring Boot API
│   └── src/main/java/com/yap/backend/
│       ├── config/       # JWT, Security, CORS
│       ├── controllers/  # REST endpoints
│       ├── dtos/         # Request/response objects
│       ├── entities/     # JPA entities
│       ├── enums/        # PostType, UserRole, etc.
│       ├── exceptions/   # GlobalExceptionHandler + custom exceptions
│       ├── keys/         # Composite keys for join tables
│       ├── repositories/ # Spring Data JPA interfaces
│       ├── security/     # JWT filter, UserDetailsService
│       └── services/     # Business logic
│
└── yap-frontend/         # React + Vite SPA
    └── src/
        ├── api.js        # Centralized API calls
        ├── components/   # Reusable UI components
        ├── hooks/        # useTheme
        └── pages/        # Route-level components
```

---

## API Endpoints

### Auth
```
POST /api/auth/register
POST /api/auth/login
```

### Spaces (Communities)
```
GET    /api/spaces               All spaces
GET    /api/spaces/my            Current user's spaces
GET    /api/spaces/{name}        Space by w/ name
POST   /api/spaces               Create space
POST   /api/spaces/{id}/join
DELETE /api/spaces/{id}/leave
```

### Posts
```
GET    /api/posts/feed/{userId}       Personalized feed
GET    /api/posts/forums/{userId}     Discussion posts only
GET    /api/posts/blogs               All blog posts
GET    /api/posts/community/{id}      Posts in a space
GET    /api/posts/{id}                Single post
POST   /api/posts                     Create post
DELETE /api/posts/{id}
GET    /api/posts/trending            Trending tags
GET    /api/posts/tag/{name}          Posts by tag
GET    /api/posts/liked-by/{userId}
GET    /api/posts/user/{userId}/community
GET    /api/posts/profile/{userId}    Profile (blog) posts
```

### Comments
```
GET    /api/posts/{postId}/comments
POST   /api/posts/comments
DELETE /api/posts/comments/{id}
POST   /api/posts/{postId}/like
DELETE /api/posts/{postId}/like
POST   /api/posts/comments/{id}/like
DELETE /api/posts/comments/{id}/like
```

### Users & Social
```
GET    /api/users/me
GET    /api/users/{username}
PATCH  /api/users/me/bio
PATCH  /api/users/me/avatar
POST   /api/users/{id}/follow
DELETE /api/users/{id}/unfollow
GET    /api/users/{id}/followers
GET    /api/users/{id}/following
```

### Notifications
```
GET    /api/notifications
GET    /api/notifications/unread-count
POST   /api/notifications/mark-all-read
```

---

## Local Development

### Prerequisites
- Java 25
- Maven 3.9+
- Node.js 18+
- PostgreSQL (local) or Neon account

### Backend

```bash
cd yap-backend
# Create application.properties (not committed — see template below)
mvn spring-boot:run
```

**`application.properties` template:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/yap
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
server.port=8080
jwt.secret=your-secret-key-min-32-characters-long
jwt.expiration=86400000
cloudinary.cloud-name=
cloudinary.api-key=
cloudinary.api-secret=
spring.task.execution.pool.core-size=4
spring.task.execution.pool.max-size=8
```

### Frontend

```bash
cd yap-frontend
npm install
npm run dev
# Runs on http://localhost:5173
# Proxies /api to http://localhost:8080
```

---

## Moderation Philosophy

Yap's moderation is three layers:

1. **Automated** — Perspective API scores every post and comment at creation. Content above 0.85 toxicity is auto-flagged for review.
2. **Community-led** — Any user can report content with a specific reason. Reports go into a mod queue visible to community moderators.
3. **Human review** — Mods review flagged/reported content and can remove it. Removal creates a notification to the author.

**No downvotes.** The mechanism that makes Reddit toxic at scale is the ability to collectively punish opinions. Upvotes surface good content. Reporting handles bad content. The mechanisms are separated by intent.

---

## Community Categories

`Career & Money` · `Health & Wellness` · `Relationships & Family` · `Identity & Life Experiences` · `Knowledge & Learning` · `Creativity & Expression` · `Hobbies & Interests` · `Lifestyle & Travel` · `Entertainment & Culture` · `Technology & Gaming` · `Q&As & Stories` · `Society & Current Issues` · `Support & Sensitive Topics` · `Other`

---

## Roadmap

**v1 (current):** Forum-first MVP — spaces, discussions, nested comments, moderation pipeline, notifications

**v2 (August hackathon):** Blog posts with rich editor, audio player feature, AI-assisted moderation v2 with context-aware classification, community brand partnerships

**Long term:** Mobile app, live audio rooms, cohort writing programs, brand partnership revenue model

---

## Built by

Daria — CS junior at CSUN, full-stack engineer, inside the target market.

> *"I'm not building this and hoping women show up. They're already asking for it in my TikTok comments."*

[@yap8co on TikTok](https://tiktok.com/@yap8co)
