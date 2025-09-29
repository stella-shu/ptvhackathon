# 🚇 Fare Guard – Inspector Ops Console (Catalyst 2025 Hackathon)

## Overview
Fare Guard is a full-stack web application designed to modernize fare 
inspection workflows for public transport.  
Inspectors can log patrols, drop pins, report incidents, and review shift 
summaries — all in real time or offline.  
The system improves efficiency, accountability, and transparency by 
replacing outdated pen-and-paper processes with a modern, interactive 
platform.

---

## ✨ Features
- **Map Dashboard** (Leaflet + OpenStreetMap)  
  - Live patrol map with support for pins and blitz zones  
  - Drop pins by clicking on the map  
  - Offline-first logging with queued events  

- **Quick Actions**  
  - Start / End Patrol (logs shifts automatically)  
  - Mark Blitz zones  
  - Drop Pin on map  
  - Log Incident (modal form with time, mode, tag, notes, photo)  
  - Upload Photo support  

- **Incident Logging**  
  - 2-tap preset forms with auto-filled time + geolocation  
  - Attach notes and photos  
  - Works offline, queues to local storage, syncs when back online  

- **Shift Summary**  
  - Shows hours patrolled and number of incidents  
  - Pops up automatically when ending a shift  

- **PWA-Ready**  
  - Installable on desktop/mobile  
  - Offline app shell via vite-plugin-pwa  

---

## 🛠 Tech Stack
- **Frontend**: React, Zustand, Tailwind, Leaflet, react-hook-form  
- **Backend (optional)**: Spring Boot + PostgreSQL + WebSocket + JWT (for 
auth & real-time updates)  
- **Build Tools**: Vite, vite-plugin-pwa  

---

## 🚀 Quick Start

1. **Database (PostgreSQL 15+)**
   - Recommended (requires Docker):
     ```bash
     cd backend/auth-logs
     docker compose up -d db
     ```
   - If Docker is not available, install Postgres locally and ensure the following credentials are valid:
     - URL `jdbc:postgresql://localhost:5432/inspector`
     - User `postgres`
     - Password `postgres`

2. **Auth & Logs backend**
   ```bash
   cd backend/auth-logs
   mvn spring-boot:run
   ```
   The service listens on `http://localhost:8080` by default and seeds `INSPECTOR1 / ChangeMe123!` for testing.

3. **Realtime maps backend** (optional during UI-only demos, but required for live blitz/chat data)
   ```bash
   cd backend/realtime-maps
   ./mvnw spring-boot:run   # or mvn spring-boot:run if the wrapper is unavailable
   ```
   This service defaults to `http://localhost:8081` and provides `/api/blitz`, `/api/location`, `/api/channels`, and `/ws`.

4. **Frontend UI**
   ```bash
   cd frontend
   npm install
   VITE_AUTH_BASE_URL=http://localhost:8080 \
   VITE_REALTIME_BASE_URL=http://localhost:8081 \
   npm run dev
   ```
   The dev server starts on `http://127.0.0.1:5174`. Log in with the seeded inspector credentials.

5. **Build artifacts (optional)** – run these only when you need to inspect the production bundle locally.
   ```bash
   npm run build   # produce dist/ assets
   npm run preview # serve the production build locally (optional)
   ```

### Backend Targets

The frontend reads service origins from environment variables so it can call the Spring Boot APIs directly or via Vite's proxy:

- `VITE_AUTH_BASE_URL` → Auth & Logs service (`/api/auth`, `/api/incidents`, `/api/shifts`)
- `VITE_REALTIME_BASE_URL` → Realtime maps service (`/api/blitz`, `/api/location`, `/api/channels`, `/ws`)

During local development you can instead export `AUTH_TARGET`, `REALTIME_TARGET`, or `PROXY_TARGET` before `npm run dev` to let Vite proxy the same routes. If none of these values are provided the app falls back to same-origin requests, so ensure the backend is hosted behind the same domain when running production builds.

## 🧱 Frontend Structure

Key frontend directories (under `frontend/src`) are organised for feature-first development:

- `app/` – application shell components and global lifecycle wiring.
- `pages/` – route-level compositions that orchestrate feature widgets.
- `features/` – feature boundaries with `components/` and `hooks/` per domain (auth, map, incidents, etc.).
  - Components live under `components/<ComponentName>/index.jsx` so containers can import `@/features/.../components/<ComponentName>` without worrying about file extensions.
- `services/` – API and realtime clients shared across features.
- `stores/` – Zustand stores the UI consumes.
- `assets/`, `styles/`, and `polyfills.js` remain for static resources and global CSS.

Import aliases are available via Vite: `@` resolves to `frontend/src` so modules can import with `@/features/...` instead of long relative paths.
