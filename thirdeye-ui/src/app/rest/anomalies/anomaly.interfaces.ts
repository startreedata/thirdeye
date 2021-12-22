import { ActionHook } from "../actions.interfaces";
import { Anomaly } from "../dto/anomaly.interfaces";

export interface GetAnomaly extends ActionHook {
    anomaly: Anomaly | null;
    getAnomaly: (anomalyId: number) => Promise<Anomaly | undefined>;
}
