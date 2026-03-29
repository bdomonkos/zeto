# Zeto Android

Android app that collects device data (battery, location, Wi-Fi) and sends it to the backend.
The UI is a React app loaded inside a WebView.

---

## Tech stack

* Java
* Dagger Hilt
* Retrofit + OkHttp
* Gson
* Google Location Services
* Timber

Min SDK 33 · Target SDK 36

---

## How it works

The app runs in the background and monitors device state.
When something changes, it sends an update to the backend.

* collects battery, location, Wi-Fi, and device info
* sends updates via `POST /api/status`
* batches quick changes (debounce ~5s)
* keeps running in background (foreground service)

If a permission is missing, the app sends `PERMISSION_DENIED` instead of the value.

---

## Structure

### Main parts

* `MainActivity`

    * hosts a WebView
    * loads the UI from `assets/index.html`

* `DeviceMonitorService`

    * runs in background
    * listens for battery, Wi-Fi, and location changes
    * triggers data collection

---

### Data flow

```id="a8z7qk"
Sensors (battery / Wi-Fi / GPS)
        ↓
DeviceMonitorService (debounce)
        ↓
Collectors (battery, wifi, location, device info)
        ↓
Repository
        ↓
Retrofit → POST /api/status
```

---

### Collectors

* `DeviceInfoCollector` → device ID, model, OS
* `BatteryCollector` → battery level + charging
* `WifiCollector` → SSID + RSSI
* `LocationCollector` → GPS location

---

## Permissions

* `ACCESS_FINE_LOCATION` → location + Wi-Fi
* `POST_NOTIFICATIONS` → foreground service
* `FOREGROUND_SERVICE` → background running
* `ACCESS_NETWORK_STATE` → network info

---

## API

* Base URL: `http://127.0.0.1:8080/`
* Endpoint: `POST /api/status`

---

## Run locally

1. Start the backend on `localhost:8080`
2. Open in Android Studio
3. Run on device
4. adb reverse tcp:8080 tcp:8080
5. Grant permissions

---

## WebView UI

The UI is loaded from:

```
app/src/main/assets/index.html
```

To update it:

1. Build the React app
2. Copy the `dist/` contents into `assets/`
