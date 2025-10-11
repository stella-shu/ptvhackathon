# Inspector Ops — Product Requirements

## Vision
Deliver a unified, real‑time command console that keeps Public Transport Victoria inspectors in sync across patrols. The product must make it effortless to visualise activity hotspots, capture incidents, coordinate blitz operations, and collaborate via secure chat while maintaining offline resilience.

## Stakeholders
- **Field Inspectors** — log incidents, share status, coordinate blitzes.
- **Shift Supervisors** — monitor patrol coverage, review reports, assign follow‑ups.
- **Operations Analysts** — analyse heatmaps, detect fare‑evasion trends, plan deployments.
- **Compliance & Audit** — ensure reporting accuracy, authentication, and traceability.

## Core User Journeys
1. **Start & End Patrol**
   - Inspector authenticates with MFA.
   - Starts a patrol session, auto-tracking duration and map presence.
   - Ends patrol to trigger summary and sync shift data.

2. **Incident Capture**
   - Log incident with mode, tag, notes, location (auto or manual).
   - Optionally attach photo metadata (future: upload).
   - Works offline; queues for sync.

3. **Blitz Coordination**
   - Drop blitz marker with description and optional schedule.
   - Share instantly via WebSocket; persists for other clients.
   - Supervisors can close blitz operations.

4. **Location Awareness**
   - Inspectors share live location (toggle by shift).
   - Heatmap overlays highlight hotspots from backend analytics.
   - Proximity alerts notify when inspectors are near one another.

5. **Team Collaboration**
   - Channel-based chat (general, incident-specific, DM).
   - System messages for joins, alerts, or automations.
   - REST fallback when WebSocket unavailable.

## Functional Requirements
- **Authentication**
  - Username + password + OTP (TOTP) via Auth & Logs service.
  - JWT issuance with inspector metadata; stored securely client-side.

- **Map & Geospatial**
  - Google Maps base layer (browser key fallback).
  - Heatmap layer for incident density.
  - Blitz + ad-hoc pins with distinct styling.

- **Offline Mode**
  - Queue incidents, blitz markers, shift events in local storage.
  - Retry automatically when online and authenticated.

- **Chat**
  - Persist messages with timestamps and geo context (optional pin).
  - Enforce input validation to mitigate abuse.
  - Rate limiting (future requirement).

- **Analytics Integration**
  - `/api/location/snapshot` returns GeoJSON features for heatmap.
  - `/api/location/active` lists current active inspectors.

## Non-Functional Requirements
- **Availability**
  - Frontend must degrade gracefully when services are unreachable.
  - Backend APIs respond with actionable error payloads.

- **Security**
  - Enforce MFA and JWT-based access control.
  - Validate and sanitise all external inputs (chat, blitz, incidents).
  - Audit logging for auth and data mutations.

- **Performance**
  - Map interactions sub-150ms after load.
  - WebSocket broadcasts under 1s end-to-end.
  - Backend endpoints respond ≤300ms median under expected load.

- **Usability**
  - Mobile-responsive UI for field use.
  - Clear feedback for loading, offline, and error states.
  - Consistent “cute” aesthetic aligning with inspector morale goals.

## Roadmap Themes (Next)
1. **Media Handling** — photo uploads, secure blob storage, thumbnails.
2. **Shift Analytics** — KPI dashboards, compliance summaries.
3. **Role-Based Access** — supervisor dashboards, admin controls.
4. **Alerting** — SMS/email escalation for high-severity incidents.
5. **Integrations** — connect to PTV legacy systems for incident sync.
