# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Forest is a modular monorepo. `business/*` provides reusable domain capability, and `apps/*` assembles many business modules into complete systems with multiple clients and backends.

## Architecture

```
forest/
├── compose.yml                   # Shared infra + app backend containers
├── business/                     # Business modules (high cohesion)
│   ├── user/
│   │   ├── backend/              # user + identity + auth + admin auth
│   │   └── frontend/             # wechat-miniapp / platform-web user capability
│   ├── lead/
│   │   ├── backend/              # lead query + admin lead management
│   │   └── frontend/             # wechat-miniapp / platform-web lead capability
│   └── point/
│       ├── backend/              # balance + logs + admin point query
│       └── frontend/             # wechat-miniapp / platform-web point capability
├── apps/                         # Application systems
│   └── trade-leads/
│       ├── backend/              # Single backend runtime (assembly + orchestration)
│       └── clients/
│           ├── platform-web/     # Platform web client
│           └── client-wechat-miniapp/ # Client WeChat miniapp
├── base-backend/                 # Backend infrastructure
│   └── starter-common/           # Common dependencies and auto-config
├── base-frontend/                # Frontend infrastructure
│   └── packages/
│       └── ui-kit/               # Base UI components (FButton, etc.)
└── docker/
    └── postgresql/               # Database-only fallback compose
```

## Tech Stack

**Backend:** JDK 25, Spring Boot 4.0.5, Spring Data JPA, PostgreSQL 18.3, Maven
**Frontend:** Vue 3.5, Vite 6, Turborepo, pnpm workspace, Changesets

## Commands

### Backend

```bash
# Build all modules
mvn clean install -DskipTests

# Run trade-leads backend locally
cd apps/trade-leads/backend
mvn spring-boot:run

# Run trade-leads backend in Docker
docker compose up --build -d postgres trade-leads-backend
```

### Frontend

```bash
cd base-frontend

# Install dependencies
pnpm install

# Dev mode (trade-leads admin web)
pnpm dev --filter @forest/trade-leads-platform-web

# Build all
pnpm turbo build
```

### Database

```bash
docker compose up -d postgres
```

## Key Patterns

### Frontend: Object Wrapper Pattern

Use composed types, not flattened data:

```typescript
interface UserProfile {
  user: User | null
  isLoading: boolean
  error: string | null
}
```

### Frontend Module Structure

- `web/auth/` - browser / PC / H5 auth capability
- `web/me/` - current browser user capability
- `web/user-management/` - browser user management capability
- `wechat-miniapp/auth/` - WeChat miniapp auth capability
- `wechat-miniapp/me/` - current miniapp user capability
- `wechat-miniapp/lead-list/` - lead list capability
- `wechat-miniapp/lead-detail/` - lead detail capability
- `wechat-miniapp/balance/` - point balance capability
- `wechat-miniapp/logs/` - point log capability
- `platform-web/lead-management/` - platform lead management capability
- `platform-web/point-query/` - platform point query capability

### Backend: Standard Response

All controllers return `Result<T>`:

```java
Result.success(data)  // code=200
Result.error(msg)     // code=500
```

### Maven GroupId Convention

- `com.forest.business.*` - Business modules (e.g., user-backend)
- `com.forest.apps.*` - Application backends (e.g., trade-leads-backend)
- `com.forest.starter.*` - Starter dependencies

### NPM Package Naming

- `@forest/user` - User module frontend
- `@forest/lead` - Lead module frontend
- `@forest/point` - Point module frontend
- `@forest/trade-leads-platform-web` - Trade-leads admin web
- `@forest/trade-leads-client-wechat-miniapp` - Trade-leads WeChat miniapp shell
- `@forest/ui-kit` - Base UI components

## Runtime Model

- One backend container per app under `apps/*`
- One shared PostgreSQL container for all apps
- `business/*` modules are packaged into app images, not deployed as separate containers

## Configuration

- **Database:** postgresql://forest:forest123@localhost:5432/forest_dev
- **Trade Leads Backend Port:** 8081
- **Node:** Volta manages Node.js 20.18.0 and pnpm 10.33.0

## Development Flow

1. Start PostgreSQL: `docker compose up -d postgres`
2. Start backend locally: `cd apps/trade-leads/backend && mvn spring-boot:run`
3. Or start backend in Docker: `docker compose up --build -d postgres trade-leads-backend`
4. Start frontend: `cd base-frontend && pnpm dev --filter @forest/trade-leads-platform-web`
