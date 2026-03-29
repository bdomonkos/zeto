import DeviceCard from "./DeviceCard";

export default function DeviceList({ devices }) {
    const sorted = [...devices].sort((a, b) => {
        const manufacturerCompare =
            (a.manufacturer ?? "").localeCompare(b.manufacturer ?? "", undefined, { sensitivity: "base" });

        if (manufacturerCompare !== 0) return manufacturerCompare;

        return (a.model ?? "").localeCompare(b.model ?? "", undefined, { sensitivity: "base" });
    });

    if (sorted.length === 0) {
        return (
            <div className="text-center py-20 text-gray-500">
                <p className="text-4xl mb-4">📱</p>
                <p className="text-lg">No devices connected yet</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-4">
            {sorted.map(device => (
                <DeviceCard key={device.deviceId} device={device} />
            ))}
        </div>
    );
}