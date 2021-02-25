import { UiAnomaly } from "../../../rest/dto/ui-anomaly.interfaces";

export interface AnomalyCardProps {
    uiAnomaly: UiAnomaly;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (uiAnomaly: UiAnomaly) => void;
}
