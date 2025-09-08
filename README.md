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

### Frontend UI
```bash
# install dependencies
npm install

# run dev server
npm run dev

# build for production
npm run build

# preview production build
npm run preview

