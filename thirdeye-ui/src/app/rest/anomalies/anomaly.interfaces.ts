import { ActionHook } from "../actions.interfaces";
import { Anomaly } from "../dto/anomaly.interfaces";

export interface GetAnomaly extends ActionHook {
    anomaly: Anomaly | null;
    getAnomaly: (anomalyId: number) => Promise<Anomaly | undefined>;
}
export interface GetAnomalies extends ActionHook {
    anomalies: Anomaly[] | null;
    getAnomalies: (
        getAnomaliesParams?: GetAnomaliesProps
    ) => Promise<Anomaly[] | undefined>;
}

export interface GetAnomaliesProps {
    alertId?: number;
    startTime?: number;
    endTime?: number;
}
