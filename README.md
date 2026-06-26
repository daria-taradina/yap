# Yap

A community-first discussion platform built for women — like reddit, but better. Moderation is report-based.

## Tech Stack

| Layer | Tech | Hosting |
|-------|------|---------|
| Frontend | React 19, Vite 8, React Router 7, TipTap (rich text) | Vercel |
| Backend | Java 25, Spring Boot 4.x, Spring Security, JPA/Hibernate | Render (free tier) |
| Database | PostgreSQL | Render (free tier) |
| Auth | JWT (jjwt 0.12.x), BCrypt | — |
| Images | Cloudinary | Free tier |
| Email | Resend (REST API, no SDK) | Free tier (100/day) |
| PWA | vite-plugin-pwa + Workbox | — |
| Testing | JUnit 5 + jqwik (property-based) | — |

## Project Structure

```
yap/
├── yap-backend/          # Spring Boot API
│   ├── src/main/java/com/yap/backend/
│   │   ├── config/       # SecurityConfig, JwtProperties
│   │   ├── controllers/  # REST controllers
│   │   ├── dtos/         # Request/response DTOs
│   │   ├── entities/     # JPA entities
│   │   ├── enums/        # PostType, ReportReason, etc.
│   │   ├── exceptions/   # Custom exceptions
│   │   ├── keys/         # Composite keys
│   │   ├── repositories/ # Spring Data repos
│   │   ├── security/     # JWT filter, auth entry point
│   │   └── services/     # Business logic
│   └── pom.xml
├── yap-frontend/         # React SPA
│   ├── src/
│   │   ├── components/   # layout/, feed/, common/, space/, profile/
│   │   ├── pages/        # FeedPage, SpacePage, ProfilePage, etc.
│   │   ├── hooks/        # Custom React hooks
│   │   ├── styles/       # tokens.css, global.css, mobile.css
│   │   ├── api.js        # API client
│   │   ├── App.jsx       # Router setup
│   │   └── main.jsx      # Entry point
│   ├── public/
│   │   ├── icons/        # PWA icons (192, 512, apple-touch-icon)
│   │   └── offline.html  # PWA offline fallback
│   ├── vite.config.js
│   └── package.json
└── .kiro/specs/          # Feature specs (requirements, design, tasks)
```

## Prerequisites

- **Java 25** (or latest JDK — project targets 25)
- **Node.js 20+** and npm
- **PostgreSQL 15+** running locally (or a remote instance)
- **Maven** (or use the included `mvnw` wrapper)

## Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/your-org/yap.git
cd yap
```

### 2. Backend Setup

```bash
cd yap-backend
```

Create `src/main/resources/application.properties` (gitignored):

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/yap
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
jwt.secret=your-256-bit-secret-key-here-make-it-long
jwt.expiration=86400000

# Cloudinary (image uploads)
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

# CORS
FRONTEND_URL=http://localhost:5173

# Actuator
management.endpoints.web.exposure.include=health

# Resend (email - optional for local dev)
resend.api-key=re_your_key_here
resend.from-email=noreply@yourdomain.com
```

Run the backend:

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

### 3. Frontend Setup

```bash
cd yap-frontend
npm install
npm run dev
```

The dev server starts at `http://localhost:5173` and proxies `/api` requests to `localhost:8080`.

### 4. Database

Create a PostgreSQL database named `yap`:

```sql
CREATE DATABASE yap;
```

Hibernate auto-creates tables on first run (`ddl-auto=update`).

## Available Scripts

### Backend

| Command | Description |
|---------|-------------|
| `./mvnw spring-boot:run` | Start dev server (port 8080) |
| `./mvnw test` | Run all tests (unit + property-based) |
| `./mvnw package -DskipTests` | Build JAR for deployment |

### Frontend

| Command | Description |
|---------|-------------|
| `npm run dev` | Start Vite dev server (port 5173) |
| `npm run build` | Production build (outputs to `dist/`) |
| `npm run preview` | Preview production build locally |
| `npm run lint` | Run ESLint |

## API Overview

All endpoints are prefixed with `/api`. Public GET access is allowed for most read endpoints; mutations require a JWT token in the `Authorization: Bearer <token>` header.

| Endpoint Group | Base Path | Auth Required |
|---------------|-----------|---------------|
| Auth | `/api/auth/**` | No |
| Posts | `/api/posts/**` | GET: No, Write: Yes |
| Communities (Spaces) | `/api/communities/**` | GET: No, Write: Yes |
| Profiles | `/api/users/**` | GET: No (except `/me`) |
| Notifications | `/api/notifications/**` | Yes |
| Health | `/actuator/health` | No |

## Architecture Decisions

- **No AI moderation** — content moderation is purely report-based. Users report → moderators review in a queue.
- **No Redis** — rate limiting uses in-memory Bucket4j. Session invalidation uses a `passwordChangedAt` timestamp compared against JWT `iat`.
- **No native app** — mobile experience is delivered via PWA (service worker, offline fallback, home screen install).
- **$0 budget** — all services run on free tiers (Vercel, Render, Resend, Giphy, Cloudinary, cron-job.org).
- **Property-based testing** — correctness properties are tested with jqwik (backend) and fast-check (frontend).

## Mobile / PWA

The frontend is a Progressive Web App:
- Responsive layout with `<BottomNav>` at ≤768px (hides sidebars and desktop nav)
- 44px minimum touch targets on mobile
- 16px font-size on inputs (prevents iOS auto-zoom)
- Service worker pre-caches the app shell
- Offline fallback page at `/offline.html`
- Installable via "Add to Home Screen" on iOS/Android

## Environment Variables (Production)

### Backend (Render)

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `JWT_SECRET` | 256-bit signing key |
| `FRONTEND_URL` | Production Vercel URL (for CORS) |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |
| `RESEND_API_KEY` | Resend email API key |

### Frontend (Vercel)

| Variable | Description |
|----------|-------------|
| `VITE_API_URL` | Backend URL (if not using Vercel rewrites) |
| `VITE_GIPHY_API_KEY` | Giphy API key (for GIF picker) |

## Keep-Alive

Render's free tier spins down after 15 min of inactivity. An external cron job on [cron-job.org](https://cron-job.org) pings `GET /actuator/health` every 14 minutes to keep the backend warm.

## Contributing

1. Create a feature branch from `main`
2. Check `.kiro/specs/yap-v1-launch/tasks.md` for the current implementation plan
3. Each task references specific requirements — follow the acceptance criteria
4. Run tests before pushing: `./mvnw test` (backend) and `npm run lint` (frontend)
5. Open a PR with a clear description of what changed

## Specs

The `.kiro/specs/` directory contains structured feature specs:
- `requirements.md` — user stories and acceptance criteria (EARS format)
- `design.md` — technical design, data models, API contracts, correctness properties
- `tasks.md` — implementation plan with dependency graph

These are the source of truth for what V1 should do.
