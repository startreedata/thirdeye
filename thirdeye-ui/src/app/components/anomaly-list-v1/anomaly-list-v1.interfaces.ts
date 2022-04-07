import { Anomaly } from "../../rest/dto/anomaly.interfaces";

export interface AnomalyListV1Props {
    anomalies: Anomaly[] | null;
    onDelete?: (anomaly: Anomaly) => void;
    searchFilterValue?: string | null;
    onSearchFilterValueChange?: (value: string) => void;
}
