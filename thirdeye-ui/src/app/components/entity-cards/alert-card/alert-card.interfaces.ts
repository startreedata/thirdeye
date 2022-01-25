import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { UiAlert } from "../../../rest/dto/ui-alert.interfaces";

export interface AlertCardProps {
    uiAlert: UiAlert | null;
    alertEvaluation: AlertEvaluation;
    searchWords?: string[];
    showViewDetails?: boolean;
    onChange?: (uiAlert: UiAlert) => void;
    onDelete?: (uiAlert: UiAlert) => void;
}
