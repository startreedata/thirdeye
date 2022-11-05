import { Alert } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";

export interface MetricsReportListProps {
    anomalies: Anomaly[] | null;
    chartStart: number;
    chartEnd: number;
}

export interface AnomaliesByAlert {
    alert: Alert;
    anomalies: Anomaly[];
    metric: string;
    dataset: string;
}
