# Myki Inspector — Realtime Maps Backend

Backend service powering **real‑time chat, location sharing, patrol zones, blitz markers, and hotspot detection** for the Myki Inspector platform.


---

## What’s Included (this repo)

- **WebSocket chat (STOMP)**  
  Channel chat (`/topic/channels/{id}`), join notices, optional map pin in messages, REST history fallback.
- **Live location sharing**  
  POST updates, broadcast to `/topic/locations`, presence = “active within last N minutes”.
- **Patrol zones**  
  CRUD-lite (create + list active), polygon boundary stored as JSON, WS broadcasts for newly created zones.
- **Blitz markers**  
  Create/list active “operations/checkpoints/sweeps”, broadcast to `/topic/blitz/created`.
- **Hotspot detection + Heatmap**  
  Scheduled clustering of recent locations; emits `/topic/hotspots/detected`, REST `/api/zones/heatmap` for heatmap points.
- **Google Maps support (optional)**  
  Reverse geocode + geocode helpers (only enabled when `GOOGLE_MAPS_API_KEY` is set).
- **Dev DB & tooling**  
  H2 in‑memory DB, JPA/Hibernate, H2 Console, Spring DevTools.
- **Scale-readiness**  
  STOMP + controller/service split, indexes on hot tables, simple caching hooks (`@EnableCaching`).

---

## Tech Stack

- **Java 17**, **Spring Boot 3.1.5**
- Starters: `web`, `websocket`, `data-jpa`, *(optional)* `cache`
- **H2** (dev); DB schema managed by JPA (ddl-auto: `update`)
- **STOMP over WebSocket** (endpoint `/ws`, prefixes `/app`, `/topic`, `/user`)
- **Google Maps Java Client** (optional, via `GOOGLE_MAPS_API_KEY`)

---

## Getting Started

### 1) Install & Run

```bash
# from project root
./mvnw spring-boot:run
```

- Base URL: `http://localhost:8080`  
- H2 Console: `http://localhost:8080/h2-console`  
  - JDBC URL: `jdbc:h2:mem:inspectordb;DB_CLOSE_DELAY=-1`  
  - User: `sa`  Password: `password` (or as per `application.yml`)

> If port 8080 is busy, set `server.port` in `src/main/resources/application.yml` or run with `--server.port=8081`.

### 2) Configuration

`src/main/resources/application.yml` (dev defaults):
- In‑memory H2, auto‑DDL, `show-sql=true`
- Open CORS in WS config (for demo)
- Google Maps (optional):
  ```yaml
  google:
    maps:
      api-key: ${GOOGLE_MAPS_API_KEY:}
  ```
Set an environment variable to enable Maps API:
```bash
export GOOGLE_MAPS_API_KEY=your_key_here
```

---

## API Reference

### WebSocket (STOMP)

- **Connect**: `ws://localhost:8080/ws` (SockJS supported)
- **Send (channel message)**: `SEND /app/channels/{channelId}/send`
- **Join channel**: `SEND /app/channels/{channelId}/join`
- **Subscribe (channel feed)**: `SUBSCRIBE /topic/channels/{channelId}`
- **Subscribe (locations)**: `SUBSCRIBE /topic/locations`
- **Subscribe (zone events)**: `SUBSCRIBE /topic/zones/created`, `/topic/zones/alerts`
- **Subscribe (blitz events)**: `SUBSCRIBE /topic/blitz/created`
- **Subscribe (hotspots)**: `SUBSCRIBE /topic/hotspots/detected`
- **Subscribe (proximity alerts)**: `SUBSCRIBE /topic/alerts/proximity`

**Chat message payload (send):**
```json
{
  "senderId": "INS001",
  "senderName": "Inspector Alice",
  "content": "On my way to platform 3",
  "pinLat": -37.8136,     // optional
  "pinLon": 144.9631,     // optional
  "type": "CHAT"          // CHAT|SYSTEM|LOCATION_ALERT|HOTSPOT_ALERT
}
```

### REST — Chat

- **Get channel history**  
  `GET /api/channels/{channelId}/messages` → `ChatMessageDto[]`
- **Send message (REST fallback)**  
  `POST /api/channels/{channelId}/messages`  
  body: same as WS payload

### REST — Location

- **Update location**  
  `POST /api/location/update`  
  ```json
  {
    "inspectorId":"INS001",
    "inspectorName":"Alice",
    "latitude":-37.8100,
    "longitude":144.9650,
    "accuracy":8,
    "active":true
  }
  ```
- **List active locations (last 10 min)**  
  `GET /api/location/active`
- **Inspector history**  
  `GET /api/location/history/{inspectorId}`
- *(Optional)* Set offline (latest point flagged inactive)  
  `POST /api/location/offline/{inspectorId}`

### REST — Zones & Heatmap

- **List active zones**  
  `GET /api/zones`
- **Create zone**  
  `POST /api/zones`  
  ```json
  {
    "name":"CBD Patrol",
    "description":"Demo zone",
    "zoneType":"patrol",
    "boundary":[
      {"lat":-37.8150,"lon":144.9550},
      {"lat":-37.8150,"lon":144.9720},
      {"lat":-37.8050,"lon":144.9720},
      {"lat":-37.8050,"lon":144.9550}
    ]
  }
  ```
- **Heatmap data (recent locations)**  
  `GET /api/zones/heatmap` → points with `lat/lng/intensity`

### REST — Blitz Markers

- **Create blitz marker**  
  `POST /api/blitz`  
  ```json
  {
    "inspectorId":"INS001",
    "latitude":-37.8122,
    "longitude":144.9660,
    "description":"Ticket blitz near exit B",
    "blitzType":"checkpoint"
  }
  ```
- **List active blitz markers**  
  `GET /api/blitz/active`

### REST — Maps Helpers (optional)

- **Reverse geocode**  
  `GET /api/maps/reverse?lat=-37.81&lon=144.96`
- **Geocode address**  
  `POST /api/maps/geocode` `{ "address": "Flinders Street Station" }`
- **Frontend map defaults**  
  `GET /api/maps/config`

---

## Quick Test Guide

### A) WebSocket chat in browser (2 tabs)

Create `ws-test.html` anywhere and open it in **two** tabs:
```html
<!doctype html><meta charset="utf-8"><title>WS Test</title>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
<button id="send">Send</button><pre id="log"></pre>
<script>
const log = m => (document.querySelector('#log').textContent += m+"\\n");
const client = Stomp.over(new WebSocket("ws://localhost:8080/ws")); client.debug=()=>{};
client.connect({}, () => {
  const ch="ops-global";
  client.subscribe("/topic/channels/"+ch, m => log("CHANNEL "+m.body));
  client.send("/app/channels/"+ch+"/join", {}, JSON.stringify({senderId:"INS001",senderName:"Alice"}));
  document.getElementById('send').onclick=()=> client.send("/app/channels/"+ch+"/send",{},JSON.stringify({
    senderId:"INS001", senderName:"Alice", content:"Hello via WS!"
  }));
});
</script>
```

Click **Send** in one tab; both should log a message.

### B) Location API (broadcasts to `/topic/locations`)

```bash
# Send location
curl -X POST http://localhost:8080/api/location/update \
  -H "Content-Type: application/json" \
  -d '{"inspectorId":"INS001","inspectorName":"Alice","latitude":-37.8100,"longitude":144.9650,"accuracy":8,"active":true}'

# Get active
curl http://localhost:8080/api/location/active

# History for one inspector
curl http://localhost:8080/api/location/history/INS001
```

### C) Zones & Hotspots

```bash
# Create a simple rectangle zone
curl -X POST http://localhost:8080/api/zones \
  -H "Content-Type: application/json" \
  -d '{
    "name":"CBD Patrol","description":"Demo","zoneType":"patrol",
    "boundary":[
      {"lat":-37.8150,"lon":144.9550},
      {"lat":-37.8150,"lon":144.9720},
      {"lat":-37.8050,"lon":144.9720},
      {"lat":-37.8050,"lon":144.9550}
    ]
  }'

# List zones
curl http://localhost:8080/api/zones

# Heatmap (recent locations as intensity points)
curl http://localhost:8080/api/zones/heatmap
```

### D) Blitz markers

```bash
# Create blitz
curl -X POST http://localhost:8080/api/blitz \
  -H "Content-Type: application/json" \
  -d '{"inspectorId":"INS002","latitude":-37.8122,"longitude":144.9660,"description":"Checkpoint near exit B","blitzType":"checkpoint"}'

# List active
curl http://localhost:8080/api/blitz/active
```

> Hotspots run via scheduler every minute; subscribe to `/topic/hotspots/detected` to see alerts as clusters form.

---

## Project Structure (key files)

```
src/main/java/com/myki/inspector/
├─ Main.java                        # @SpringBootApplication
├─ config/
│  ├─ WebSocketConfig.java          # STOMP endpoints + broker
│  ├─ SchedulingConfig.java         # @EnableScheduling
│  ├─ GoogleMapsConfig.java         # Optional Maps (via API key)
│  └─ CacheConfig.java              # @EnableCaching + caches
├─ controller/
│  ├─ ChatController.java
│  ├─ LocationController.java
│  ├─ BlitzController.java
│  ├─ MapsController.java
│  └─ HotspotController.java (if present; or combined in ZoneController)
├─ dto/
│  ├─ ChatMessageDto.java
│  ├─ LocationUpdateDto.java
│  └─ PatrolZoneDto.java
├─ entity/
│  ├─ ChatMessage.java
│  ├─ LocationUpdate.java
│  ├─ BlitzMarker.java
│  └─ PatrolZone.java
├─ repository/
│  ├─ ChatMessageRepository.java
│  ├─ LocationUpdateRepository.java
│  ├─ BlitzMarkerRepository.java
│  └─ PatrolZoneRepository.java
└─ service/
   ├─ ChatService.java
   ├─ LocationService.java
   ├─ BlitzService.java
   ├─ PatrolZoneService.java
   └─ HotspotService.java
```

---

