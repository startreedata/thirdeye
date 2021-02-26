import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";

export interface AnomalyListProps {
    hideSearchBar?: boolean;
    uiAnomalies: UiAnomaly[] | null;
    onDelete?: (uiAlert: UiAnomaly) => void;
}
