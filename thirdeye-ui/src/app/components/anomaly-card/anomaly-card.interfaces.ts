import { Anomaly } from "../../rest/dto/anomaly.interfaces";

export interface AnomalyCardProps {
    anomaly: Anomaly;
    searchWords?: string[];
}
