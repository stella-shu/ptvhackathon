# Inspector Ops — Frontend

## Quick Start

- Install deps: `npm ci`
- Dev server: `npm run dev`
- Build: `npm run build`
- Preview build: `npm run preview`

If your environment blocks IPv6/::1 binding, use:

`npm run dev -- --host 127.0.0.1 --port 5174`

## Google Maps API Key

Create `.env.local` and set:

`VITE_GOOGLE_MAPS_API_KEY=your_key_here`

Alternatively, let the backend provide the key by exporting `GOOGLE_MAPS_BROWSER_API_KEY` (or setting `google.maps.browser-api-key` in the Spring config) before starting `realtime-maps-backend`; the frontend will auto-detect it via `/api/maps/config`.

Without a valid key from either source, the map will not load and a notice will remain on screen.

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

## PWA Build

The PWA layer is disabled by default during builds to avoid CI/sandbox issues.
To enable PWA generation, run:

`BUILD_PWA=true npm run build`

## Documentation

- Product requirements: `docs/product-requirements.md`
- Technical analysis & architecture notes: `docs/technical-analysis.md`
- User guide & UX backlog: `docs/user-guide.md`

---

# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
