# Myki Inspector — Realtime Maps Backend

Backend service for the Myki Inspector platform providing real-time chat and (future) location tracking.
Built with Java 17 + Spring Boot, WebSocket/STOMP, and H2 for development.

---

## Features Implemented ✅

### Chat System
- Real-time messaging via WebSocket with STOMP protocol
- Channel-based chat (group channels like ops-global) and support for future DMs
- Message persistence with JPA/Hibernate to H2 (in-memory, dev)
- REST API for message history and HTTP-based messaging
- Message types: Chat, System notifications, Location alerts, Hotspot alerts

### Database
- H2 in-memory database for development
- JPA/Hibernate for data persistence
- Proper indexing on (channel_id, created_at) for fast history queries
- H2 console available for debugging

---

## Tech Stack

- Java 17, Spring Boot (Web, WebSocket, JPA)
- STOMP over WebSocket
- H2 (dev), switchable to PostgreSQL later
- Spring DevTools (optional)

---

## Run Locally

From project root:

    ./mvnw spring-boot:run

- Base URL: http://localhost:8080
- WebSocket endpoint: ws://localhost:8080/ws
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:mem:inspectordb;DB_CLOSE_DELAY=-1
  - User: sa, Password: password (or as set in application.yml)

Tip: If port 8080 is taken, set server.port in src/main/resources/application.yml or run with --server.port=8081.

---

## API Endpoints

### REST APIs

#### Get Channel Message History
GET /api/channels/{channelId}/messages
Returns: Array of ChatMessageDto

Example:
    curl http://localhost:8080/api/channels/ops-global/messages

#### Send Message via REST
POST /api/channels/{channelId}/messages
Content-Type: application/json

Body:
    {
      "senderId": "INS001",
      "senderName": "Inspector Name",
      "content": "Message content",
      "pinLat": -37.8136,  
      "pinLon": 144.9631   
    }

Example:
    curl -X POST http://localhost:8080/api/channels/ops-global/messages \
      -H "Content-Type: application/json" \
      -d '{"senderId":"INS001","senderName":"Inspector Name","content":"Hello from REST"}'

---

### WebSocket (STOMP) APIs

#### Connection Endpoint
- ws://localhost:8080/ws

#### Send Message
SEND /app/channels/{channelId}/send
Payload:
    {
      "senderId": "INS001",
      "senderName": "Inspector Name",
      "content": "Message content"
    }

#### Join Channel
SEND /app/channels/{channelId}/join
Payload:
    {
      "senderId": "INS001",
      "senderName": "Inspector Name"
    }

#### Subscribe to Channel
SUBSCRIBE /topic/channels/{channelId}

Minimal browser test (WS + STOMP):
    <!doctype html>
    <meta charset="utf-8">
    <title>WS Test</title>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <script>
    const ws = new WebSocket("ws://localhost:8080/ws");
    const client = Stomp.over(ws);
    client.connect({}, () => {
      const channelId = "ops-global";
      client.subscribe("/topic/channels/" + channelId, (msg) => {
        console.log("RECV", JSON.parse(msg.body));
      });
      client.send("/app/channels/" + channelId + "/send", {}, JSON.stringify({
        senderId: "INS001",
        senderName: "Inspector Name",
        content: "Hello team (via WS)!"
      }));
    });
    </script>
    <!-- Open the browser devtools console to see messages. -->

---

## Data Model

ChatMessage (entity):
- id: Long
- channelId: String (e.g., ops-global, dm:INS001:INS007)
- senderId: String (Inspector ID)
- senderName: String
- content: String (<=2000)
- pinLat, pinLon: Double? (optional map pin)
- type: Enum { CHAT, SYSTEM, LOCATION_ALERT, HOTSPOT_ALERT }
- createdAt: Instant

ChatMessageDto (outbound example):
    {
      "id": 123,
      "channelId": "ops-global",
      "senderId": "INS001",
      "senderName": "Inspector Name",
      "content": "Hello team!",
      "pinLat": -37.8136,
      "pinLon": 144.9631,
      "type": "CHAT",
      "ts": "2025-09-06T02:10:00Z"
    }

---

## Project Structure (key files)

    src/main/java/com/myki/inspector/
    ├─ Main.java                         # @SpringBootApplication entrypoint
    ├─ config/
    │  └─ WebSocketConfig.java           # STOMP endpoints + broker
    ├─ controller/
    │  └─ ChatController.java            # WS @MessageMapping + REST
    ├─ dto/
    │  └─ ChatMessageDto.java
    ├─ entity/
    │  └─ ChatMessage.java
    ├─ repository/
    │  └─ ChatMessageRepository.java
    └─ service/
       └─ ChatService.java

---

