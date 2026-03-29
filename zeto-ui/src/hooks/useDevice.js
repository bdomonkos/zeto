import { useEffect, useState } from "react";
const API_URL = import.meta.env.VITE_API_URL;

const STORAGE_KEY = "devices";

const loadCache = () => {
    try {
        return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
    } catch {
        return [];
    }
};

const saveCache = (devices) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(devices));
};

export function useDevices() {
    const [devices, setDevices] = useState(loadCache());
    const [isOnline, setIsOnline] = useState(navigator.onLine);

    useEffect(() => {
        const goOnline = () => setIsOnline(true);
        const goOffline = () => setIsOnline(false);

        window.addEventListener("online", goOnline);
        window.addEventListener("offline", goOffline);

        return () => {
            window.removeEventListener("online", goOnline);
            window.removeEventListener("offline", goOffline);
        };
    }, []);

    useEffect(() => {
        if (!isOnline) return;

        fetch(`${API_URL}/api/devices`)
            .then(res => res.json())
            .then(data => {
                setDevices(sortDevices(data));
                saveCache(data);
            })
            .catch(() => {
            });
    }, [isOnline]);

    useEffect(() => {
        if (!isOnline) return;

        let ws;
        let retryTimeout;

        const connect = () => {
            ws = new WebSocket(`${API_URL.replace("http", "ws")}/ws`)

            ws.onopen = () => {
                console.log("WS connected");
            };

            ws.onmessage = (event) => {
                console.log("WS RAW:", event.data);

                const updated = JSON.parse(event.data);
                console.log("WS PARSED:", updated);

                setDevices(prev => {
                    const exists = prev.find(d => d.deviceId === updated.deviceId);

                    let next;

                    if (exists) {
                        next = prev.map(d =>
                            d.deviceId === updated.deviceId ? updated : d
                        );
                    } else {
                        next = [...prev, updated];
                    }

                    saveCache(next);
                    return sortDevices(next);
                });
            };

            ws.onclose = () => {
                console.log("WS disconnected → retrying...");
                retryTimeout = setTimeout(connect, 3000);
            };

            ws.onerror = () => {
                ws.close();
            };
        };

        connect();

        return () => {
            ws?.close();
            clearTimeout(retryTimeout);
        };
    }, [isOnline]);

    return { devices, isOnline };
}

const sortDevices = (devices) => {
    return [...devices].sort((a, b) => {
        const aKey = `${a.manufacturer} ${a.model}`.toLowerCase();
        const bKey = `${b.manufacturer} ${b.model}`.toLowerCase();
        return aKey.localeCompare(bKey);
    });
};