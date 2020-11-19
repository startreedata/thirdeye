import { Anomaly } from "../../rest/dto/anomaly.interfaces";

export interface AnomalyCardProps {
    data: Anomaly;
    mode: "detail" | "list";
}
