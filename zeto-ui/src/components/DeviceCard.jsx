import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";

dayjs.extend(relativeTime);

const isStale = (timestamp) => {
    return dayjs().diff(dayjs(timestamp), "hour") >= 12;
};

const Row = ({ label, value }) => (
    <div className="flex justify-between items-center py-1 border-b border-gray-800 last:border-0">
        <span className="text-gray-400 text-sm">{label}</span>
        <span className="text-gray-200 text-sm font-medium">{value ?? "N/A"}</span>
    </div>
);

export default function DeviceCard({ device }) {
    const stale = isStale(device.timestamp);

    return (
        <div className={`rounded-xl p-5 border ${
            stale
                ? "bg-yellow-950 border-yellow-700"
                : "bg-gray-900 border-gray-800"
        }`}>
            <div className="flex items-center justify-between mb-4">
                <div>
                    <h2 className="text-lg font-semibold text-white">
                        {device.manufacturer} {device.model}
                    </h2>
                    <p className="text-gray-500 text-xs mt-0.5">ID: {device.deviceId}</p>
                </div>
                <div className="flex flex-col items-end gap-1">
                    {stale && (
                        <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-900 text-yellow-400">
              ⚠ No data for 12h+
            </span>
                    )}
                    <span className="text-gray-500 text-xs">
            {dayjs(device.timestamp).fromNow()}
          </span>
                </div>
            </div>

            <div className="flex flex-col gap-0.5">
                <Row label="OS Version" value={device.osVersion} />
                <Row label="Battery" value={device.batteryLevel != null ? `${device.batteryLevel}%` : device.batteryLevel} />
                <Row label="Charging" value={device.charging != null ? (device.charging ? "Yes" : "No") : device.charging} />
                <Row
                    label="Location"
                    value={
                        device.latitude === "PERMISSION_DENIED" || device.longitude === "PERMISSION_DENIED"
                            ? "PERMISSION_DENIED"
                            : device.latitude != null && device.longitude != null
                                ? `${device.latitude}, ${device.longitude}`
                                : "N/A"
                    }
                />
                <Row label="WiFi SSID" value={device.wifiSsid} />
                <Row
                    label="WiFi RSSI"
                    value={
                        device.wifiRssi === "PERMISSION_DENIED" || device.wifiRssi === "WIFI_DISABLED"
                            ? device.wifiRssi
                            : device.wifiRssi != null
                                ? `${device.wifiRssi} dBm`
                                : "N/A"
                    }
                />
            </div>
        </div>
    );
}