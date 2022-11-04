import { AnomaliesByAlert } from "../metrics-report-list.interfaces";

export interface MetricsReportRowProps {
    anomalyAlert: AnomaliesByAlert;
    chartStart: number;
    chartEnd: number;
}
