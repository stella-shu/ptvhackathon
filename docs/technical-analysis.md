# Technical Analysis — Inspector Ops Platform

## System Overview
The platform comprises a React + Vite frontend and two Spring Boot microservices:

1. **Frontend (src/)** — real-time map dashboard, incident forms, chat UI, offline queue.
2. **Auth & Logs Backend** — authentication, shift/incident persistence, audit logging.
3. **Realtime Maps Backend** — location updates, WebSocket topics, blitz coordination, chat history, geospatial analytics.

Shared concerns include JWT propagation, Google Maps integration, and WebSocket connectivity.

## Architecture Diagram (Narrative)
- React app communicates with both backends through REST endpoints proxied via Vite.
- WebSocket connections (`/ws`) deliver chat messages, location updates, blitz events.
- Auth service issues JWT on login; frontend stores `auth` object in localStorage and attaches `Authorization` header to API calls.
- Realtime backend uses STOMP with SockJS fallback for clients.

## Key Modules
| Area | File(s) | Notes |
| --- | --- | --- |
| Offline queue | `src/store/useAppStore.js` | LocalStorage-backed queue; handles shift, incident, blitz actions. |
| Map rendering | `src/components/MapDashboard.jsx` | Auto-fetches Google Maps key, loads heatmap + pins, handles drop-pin mode. |
| Chat | `src/components/ChatPanel.jsx`, `realtime-maps-backend/src/main/java/.../ChatService.java` | WebSocket-first with REST fallback; validation added server-side. |
| Auth | `src/components/LoginModal.jsx`, `Auth & Logs Backend ... AuthService.java` | MFA enforced, JWT response includes inspector metadata. |
| Heatmap | `realtime-maps-backend/src/main/java/.../HotspotService.java` | Aggregates location updates for client heatmap snapshot. |

## Strengths
- **Resilient Offline Mode** — queue pattern with DTO mappers ensures data is never lost when offline.
- **Consistent Styling Theme** — shared class tokens in `src/lib/theme.js` unify UI refresh.
- **Backend Validation Hardening** — DTO annotations plus service-level guards reduce malformed data risk.
- **Google Maps Key Handling** — backend-supplied browser key removes manual frontend configuration burden.

## Risks & Gaps
1. **Lack of Rate Limiting** — Chat and location update endpoints vulnerable to spam; consider Spring throttling or token bucket.
2. **No End-to-End Tests** — No automated regression coverage for offline queue, chat flows, or map interactions.
3. **LocalStorage Persistence** — Queue size unrestricted; add eviction policy or compression for long patrols.
4. **Geospatial Accuracy** — LocationService blindly trusts coordinates; consider integrating reverse-geocode validation or server-side filtering.
5. **Security** — JWT verification not shown in realtime backend controllers; ensure Spring Security filters applied.
6. **Media Upload Placeholder** — Incident photo capture currently metadata only; need secure upload pipeline to avoid broken UX.

## Performance Considerations
- Heatmap requests invoked on map load; caching in `HotspotService` reduces DB load, but consider TTL-based invalidation.
- WebSocket subscriptions may duplicate when ChatPanel toggled; ensure `getClient()` connection reuse continues to prevent connection storms.
- Frontend map uses `HeatmapLayer` with radius 36: tune for performance on large datasets.

## Deployment Notes
- **Environment**
  - Frontend expects `VITE_API_BASE_URL` for production.
  - Backend must set `GOOGLE_MAPS_API_KEY` and optionally `GOOGLE_MAPS_BROWSER_API_KEY`.
  - Auth service seeds bootstrap inspector via `BootstrapConfig`.
- **Build**
  - `npm run build` generates optimized assets; ensure `BUILD_PWA` toggled intentionally.
  - Spring Boot services packaged with Maven (pom.xml present).
- **Logging & Monitoring**
  - Logging set to DEBUG for `com.myki.inspector`; adjust in prod.
  - Consider exposing Prometheus metrics and WebSocket session stats.

## Future Enhancements
1. **Service Contracts**
   - Create OpenAPI specs for both backends; generate typed clients for frontend.
2. **State Synchronisation**
   - Implement optimistic updates for chat and blitz operations with rollback on failure.
3. **Modular Auth**
   - Introduce refresh tokens and session revocation.
4. **Automated QA**
   - Add Cypress/Playwright scenarios covering offline/online transitions, map overlays, chat.
5. **Map UX**
   - Persist user map preferences (zoom, toggles) per inspector via backend profile.
