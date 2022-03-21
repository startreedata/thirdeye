import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { UiAlert } from "../../../rest/dto/ui-alert.interfaces";

export interface AlertCardProps {
    uiAlert: UiAlert | null;
    anomalies: Anomaly[] | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onChange?: (uiAlert: UiAlert) => void;
    onDelete?: (uiAlert: UiAlert) => void;
}
