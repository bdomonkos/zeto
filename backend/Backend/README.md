# Zeto Backend

Spring Boot backend that collects device data and pushes updates to connected clients in real time.

---

## Tech stack

* Spring Boot
* Spring Web (REST API)
* Spring WebSocket
* Spring Validation
* Lombok
* Jackson
* SLF4J / Logback
* JUnit

Java 17 · Gradle

---

## How it works

Devices send their current state to the backend.
The server stores the latest version and notifies all connected clients when something changes.

* `POST /api/status` → device sends data
* `GET /api/devices` → get all devices
* WebSocket `/ws` → live updates

---

## Architecture

```
Controller → Service → DeviceStore (interface) → In-memory store (ConcurrentHashMap)
                       ↓
                  WebSocket → clients
```

### Structure

* `controller/` → REST endpoints
* `service/` → main logic + device handling
* `repository/` → storage layer (interface + implementation)
* `model/` → device data
* `dto/` → request objects
* `websocket/` → pushes updates
* `config/` → WebSocket
* `exception/` → error handling

---

## Storage

Data is stored in memory via `InMemoryDeviceStore`, which implements the `DeviceStore` interface.

* The `DeviceStore` abstraction allows replacing the storage implementation in the future (e.g., database, file).
* In this small demo, using an interface is arguably overengineering, but it illustrates clean architecture principles.


No database is used, so data is lost on restart.

---

## Update logic

An update is only accepted if it’s newer than the stored one (`timestamp` check).
Older or duplicate data is ignored.

---

## API

### POST `/api/status`

Send device data:

```json
{
  "deviceId": "abc-123",
  "manufacturer": "Samsung",
  "model": "Galaxy S24",
  "osVersion": "14",
  "batteryLevel": 87,
  "charging": false,
  "latitude": 47.4979,
  "longitude": 19.0402,
  "wifiSsid": "HomeNetwork",
  "wifiRssi": -62,
  "timestamp": 1711618800000,
  "missingPermissions": []
}
```

Validation:

* `deviceId` required
* `batteryLevel` 0–100
* `timestamp` must be positive

---

### GET `/api/devices`

Returns all devices:

```json
[
  {
    "deviceId": "abc-123",
    "manufacturer": "Samsung",
    "model": "Galaxy S24",
    "osVersion": "14",
    "batteryLevel": 87,
    "charging": false,
    "latitude": 47.4979,
    "longitude": 19.0402,
    "wifiSsid": "HomeNetwork",
    "wifiRssi": -62,
    "lastSeen": 1711618800000,
    "missingPermissions": []
  }
]
```

The list is sorted by manufacturer and model.
CORS is enabled for all origins.

---

## WebSocket

`ws://<host>/ws`

Whenever a device sends new data, the server pushes the updated state to all connected clients.

Clients only need to connect and listen.

---

## Run locally

```bash
./gradlew bootRun
```

Runs on port **8080** by default.


Run with Docker

A Dockerfile is included, so the backend can also be run in a container:
# Build the Docker image
docker build -t zeto-backend .
# Run the container
docker run -p 8080:8080 zeto-backend
