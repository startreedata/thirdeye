import { AnomalyFeedbackType } from "../../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../../rest/dto/ui-anomaly.interfaces";

export interface AnomalyCardProps {
    uiAnomaly: UiAnomaly | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (uiAnomaly: UiAnomaly) => void;
    onFeedback?: (type: AnomalyFeedbackType, uiAnomaly: UiAnomaly) => void;
}
