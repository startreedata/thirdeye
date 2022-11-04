import { Alert } from "../../rest/dto/alert.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";

export interface AlertListV1Props {
    alerts: UiAlert[] | null;
    onDelete?: (uiAlert: UiAlert) => void;
    onAlertReset?: (alert: Alert) => void;
}
