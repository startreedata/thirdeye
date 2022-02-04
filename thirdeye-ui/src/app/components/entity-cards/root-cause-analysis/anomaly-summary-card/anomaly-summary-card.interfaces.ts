import { UiAnomaly } from "../../../../rest/dto/ui-anomaly.interfaces";

export interface AnomalySummaryCardProps {
    uiAnomaly: UiAnomaly | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (uiAnomaly: UiAnomaly) => void;
}
