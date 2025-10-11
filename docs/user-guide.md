# Inspector Ops — User Guide & UX Opportunities

## Getting Started
1. **Sign In**
   - Launch the web app.
   - Enter inspector ID, password, and OTP from your authenticator app.
   - Upon success, you’ll see your name in the top-right status pill.

2. **Prepare for Patrol**
   - Review heatmap and active inspector list on the map.
   - Open the Chat panel to join the default `general` channel.
   - Toggle overlays (Heatmap, Active) via the top-left control bar.

3. **Start a Shift**
   - Click “🌼 Start Patrol” in the bottom-right Quick Actions.
   - The app tracks your shift start time and enables incident logging.

## Field Operations
### Dropping Pins & Blitzes
- **Drop Pin**: enable drop mode, click map to add a local marker (visible only to you).
- **Mark Blitz**: adds a shared blitz marker and syncs with the backend; appears for all inspectors.

### Logging Incidents
1. Hit “📝 Log Incident”.
2. Select transport mode and incident tag.
3. Add notes and optional photo (metadata stored for now).
4. Confirm; you’ll see a toast and the incident queues for sync.

### Chat & Collaboration
- Open chat via “Open Chat”.
- Switch channels by editing the channel input (e.g. `ops-city-loop`).
- Press Enter to send; offline posts fall back to REST.

### Ending Patrol
- Tap “🌙 End Patrol” to close the shift.
- Review the shift summary modal (hours, incidents recorded).
- Close the summary to reset for the next patrol.

## Offline Behaviour
- When connectivity drops, actions queue automatically.
- The queue flushes when you regain connection or sign back in.
- Status updates appear subtly in the UI; consider checking network icon if sync is delayed.

## Troubleshooting
| Symptom | Check | Fix |
| --- | --- | --- |
| Map shows “Checking configuration…” | Ensure backend provides `GOOGLE_MAPS_BROWSER_API_KEY` or frontend `.env` has key. | Set environment variable, restart backend. |
| Incident form stuck on “Getting location…” | Browser location services disabled. | Enable location permissions; fallback to manual note. |
| Chat messages not sending | WebSocket offline. | Stay on page; REST fallback pushes message, or reload once network stabilises. |
| Shift not syncing | Ensure you’re signed in and online, then toggle Airplane mode off/on. | Queue flushes automatically; manual triggers via dev console `useAppStore.getState().flushQueue()`. |

## UX & Feature Optimisation Backlog
1. **Incident Media Uploads**
   - Implement secure photo/video upload with thumbnails and moderation queue.
2. **Map Personalisation**
   - Save preferred overlays, zoom, and map theme per inspector profile.
3. **Chat Quality of Life**
   - Typing indicators, @mentions, and canned responses for quick alerts.
4. **Shift Insights**
   - Add timeline view of incidents during shift, exportable as PDF summary.
5. **Accessibility**
   - Keyboard shortcuts for Quick Actions, improved contrast for glassmorphism elements.
6. **Mobile Optimisation**
   - Adaptive layouts for iOS/Android devices with larger touch targets.
7. **Notification Layer**
   - Toasts/notifications for proximity alerts, new blitz markers, and urgent incidents.
8. **Queue Visibility**
   - UI widget showing pending offline items with manual retry/clear controls.

## Administrator Tips
- Configure Google Maps browser key via `GOOGLE_MAPS_BROWSER_API_KEY`.
- Rotate JWT signing secrets regularly; ensure backend property `jwt.secret` is managed securely.
- Monitor chat volume; consider enabling rate limiting (`Bucket4j`, Redis) for production.
- Review audit logs (Auth & Logs backend) to track authentication failures or suspicious activity.
