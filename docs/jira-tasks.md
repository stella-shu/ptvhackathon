# JIRA Backlog Seeds

## INS-201 — Offline Queue Visibility Widget
- **Summary**: Surface pending offline items so inspectors know when data is still syncing.
- **Description**: Add a pill widget near the Quick Actions tray that shows the count of queued incidents/blitz updates. Provide manual “retry” and “clear” controls with confirmation dialogue. State must persist per session and disappear when queue empties.
- **Acceptance Criteria**:
  1. Widget appears only when `offlineQueue` in localStorage has items.
  2. “Retry now” invokes `flushQueue` and displays success/failure toast.
  3. “Clear queue” removes entries after confirmation and logs an audit event.
- **Dependencies**: Requires exposing `flushQueue` and a new `clearQueue` helper in `useAppStore`. Coordinate audit logging with Auth & Logs backend.

## INS-202 — Chat Rate Limiting & Abuse Guardrails
- **Summary**: Prevent chat spam by throttling messages per inspector and hardening channel validation.
- **Description**: Implement server-side rate limiting (token bucket, e.g. Bucket4j) allowing 5 messages per 10 seconds. Excess messages should receive HTTP 429/WS error payload. Maintain audit trail of blocked attempts and expose metrics for monitoring.
- **Acceptance Criteria**:
  1. REST and WebSocket chat sends share the same throttle.
  2. Clients receive explicit error payload with `retryAfter` hint.
  3. Metrics published via Micrometer (`chat.messages.rateLimited` counter).
- **Dependencies**: Align with security to ensure JWT identity is available; update frontend to surface rate-limit toast.

## INS-203 — Incident Photo Upload Pipeline
- **Summary**: Allow attaching photos to incident reports with secure storage.
- **Description**: Extend frontend form to capture File objects, upload to temporary object storage (e.g., S3 pre-signed URL), store returned blob URL in incident payload. Backend must validate MIME type/size, persist metadata, and schedule background virus scan.
- **Acceptance Criteria**:
  1. Inspectors can add JPEG/PNG up to 10 MB; larger files rejected client-side.
  2. Incident API persists photo metadata and associates storage key.
  3. Audit log records upload attempt result; failing scans mark incident as `REVIEW`.
  4. Documentation updated with storage retention policy.
- **Dependencies**: Requires selecting storage provider, enabling signed URL issuance, and provisioning virus scanning (ClamAV/Lambda).

## INS-204 — Heatmap Refresh Interval Optimisation
- **Summary**: Reduce redundant backend calls while keeping heatmap fresh.
- **Description**: Introduce an adaptive refresh strategy that polls `/api/location/snapshot` every 60s by default, speeding up to 15s when fresh incidents arrive. Cache snapshots client-side to avoid flicker.
- **Acceptance Criteria**:
  1. Polling interval adapts based on incident volume (configurable thresholds).
  2. Map refresh does not block user interactions (use idle callbacks).
  3. Backend metrics show ≥30% reduction in snapshot calls during low traffic.
- **Dependencies**: Coordination with analytics team to accept `If-Modified-Since` headers or ETag support for snapshot endpoint.
