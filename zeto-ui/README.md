# Zeto UI

A simple React dashboard that shows the status of Android devices in real time.  
It also works offline by keeping the last known data.

---

## Tech stack

- React
- Vite
- Tailwind
- dayjs

---

## How it works

The app loads the device list from the backend, then listens for updates via WebSocket.

- on startup → fetch all devices
- when new data arrives → update the list
- if offline → show cached data

---

## Structure

- `App` → main layout
- `DeviceList` → renders the list
- `DeviceCard` → single device

All logic is handled in the `useDevice` hook:
- API calls
- WebSocket connection
- localStorage caching

---

## UI

Each device is displayed as a card with:

- manufacturer + model
- device ID
- Android version
- battery level + charging state
- location
- WiFi info
- last seen timestamp

Devices that haven’t reported for **12+ hours** are highlighted in yellow.

---

## API

- `GET /api/devices` → full list
- WebSocket `/ws` → real-time updates

---

## Run locally

```bash
npm install
npm run dev