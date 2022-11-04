import { UiAnomaly } from "../../../rest/dto/ui-anomaly.interfaces";

export interface AnomalyCardProps {
    uiAnomaly: UiAnomaly | null;
    searchWords?: string[];
    className?: string;
    isLoading?: boolean;
}
