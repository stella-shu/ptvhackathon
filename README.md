# Inspector Ops — Frontend

## Quick Start

- Install deps: `npm install`  (first run after switching to Leaflet)
- Dev server: `npm run dev`
- Build: `npm run build`
- Preview build: `npm run preview`

If your environment blocks IPv6/::1 binding, use:

`npm run dev -- --host 127.0.0.1 --port 5174`

## Map Provider

The app now uses OpenStreetMap tiles via Leaflet. No API key is required.

## Backend API (optional)

If you want to sync incidents and shift events to the backend, set:

`VITE_API_BASE_URL=https://your-backend-host`

Queued items (incidents, shift start/end) will be POSTed to:
- `/api/incidents`
- `/api/shifts` (create on start)
- `/api/shifts/{id}` (update on end)

If the base URL is missing or requests fail, items remain in the offline queue and will retry when you go online.

### Dev proxy to avoid CORS

Instead of setting a base URL, you can rely on Vite’s dev proxy. Set the backend URL in `VITE_API_BASE_URL` and start the dev server; Vite will proxy `/api/*` to that URL. Requests in the browser stay on the same origin and avoid CORS.

WebSockets are also proxied: the dev server forwards `/ws` to the backend and upgrades to WebSocket.

### Merge two backends behind the dev server

During development you can run both backends and let Vite proxy to each one under the same origin:

1) Start the two backends on different ports:
- Realtime Maps Backend: `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` in `realtime-maps-backend`
- Auth & Logs Backend: `docker compose up -d db && mvn spring-boot:run` in `Auth & Logs Backend ` (defaults to 8080)

2) Create `.env.local` in the project root with:
```
AUTH_TARGET=http://localhost:8080
REALTIME_TARGET=http://localhost:8081
```
Do not set `VITE_API_BASE_URL` so the app uses relative paths and the dev proxy.

3) Run the frontend: `npm run dev` (http://127.0.0.1:5174)

Routes mapped by the dev proxy:
- `/api/auth`, `/api/incidents`, `/api/shifts` → Auth & Logs Backend
- `/api/blitz`, `/api/channels`, `/api/location`, `/ws` → Realtime Maps Backend

Shortcut: run everything with one command
- `npm run dev:all`
  - Creates `.env.local` with `AUTH_TARGET` and `REALTIME_TARGET` if missing
  - Starts Postgres (Docker) for Auth & Logs backend
  - Launches both Spring Boot backends (8080 and 8081) in background
  - Starts the frontend dev server in foreground

## PWA Build

The PWA layer is disabled by default during builds to avoid CI/sandbox issues.
To enable PWA generation, run:

`BUILD_PWA=true npm run build`

---

# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
