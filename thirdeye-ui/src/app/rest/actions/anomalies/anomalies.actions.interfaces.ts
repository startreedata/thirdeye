import { Anomaly } from "../../dto/anomaly.interfaces";
import { ActionHook } from "../actions.interfaces";

export interface FetchAnomaly extends ActionHook {
    anomaly: Anomaly | null;
    fetchAnomaly: (id: number) => Promise<void>;
}

export interface FetchAllAnomalies extends ActionHook {
    anomalies: Anomaly[] | null;
    fetchAllAnomalies: () => Promise<void>;
}

export interface FetchAnomaliesByAlertId extends ActionHook {
    anomalies: Anomaly[] | null;
    fetchAnomaliesByAlertId: (alertId: number) => Promise<void>;
}

export interface FetchAnomaliesByTime extends ActionHook {
    anomalies: Anomaly[] | null;
    fetchAnomaliesByTime: (startTime: number, endTime: number) => Promise<void>;
}

export interface FetchAnomaliesByAlertIdAndTime extends ActionHook {
    anomalies: Anomaly[] | null;
    fetchAnomaliesByAlertIdAndTime: (
        alertId: number,
        startTime: number,
        endTime: number
    ) => Promise<void>;
}

export interface DeleteAnomaly extends ActionHook {
    deleteAnomaly: (id: number) => Promise<void>;
}
