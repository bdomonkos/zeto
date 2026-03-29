
import {useDevices} from "./hooks/useDevice.js";
import DeviceList from "./components/DeviceList.jsx";
import dayjs from "dayjs";


export default function App() {
  const { devices, isOnline } = useDevices();


  return (
      <div className="min-h-screen bg-black text-white">
        <div className="max-w-3xl mx-auto p-4">

          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold">
              Device Dashboard
            </h1>

            <span
                className={`text-xs px-3 py-1 rounded-full font-medium ${
                    isOnline
                        ? "bg-green-900 text-green-400"
                        : "bg-red-900 text-red-400"
                }`}
            >
                        {isOnline ? "Online" : "Offline"}
                    </span>
          </div>

          <DeviceList devices={devices} />

        </div>
      </div>
  );
}