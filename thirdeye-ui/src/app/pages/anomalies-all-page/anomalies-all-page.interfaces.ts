import { GetAnomaliesProps } from "../../rest/anomalies/anomaly.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";

export interface AnomaliesAllPageContext {
    anomalies: Anomaly[];
    handleAnomalyDelete: (uiAnomalies: UiAnomaly[]) => void;
    anomalyFilters: GetAnomaliesProps;
}
