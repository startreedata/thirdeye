import { UiAlert } from "../../rest/dto/ui-alert.interfaces";

export interface AlertListV1Props {
    alerts: UiAlert[] | null;
    onChange?: (uiAlert: UiAlert) => void;
    onDelete?: (uiAlert: UiAlert) => void;
}
