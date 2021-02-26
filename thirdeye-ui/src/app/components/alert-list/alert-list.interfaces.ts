import { UiAlert } from "../../rest/dto/ui-alert.interfaces";

export interface AlertListProps {
    hideSearchBar?: boolean;
    uiAlerts: UiAlert[] | null;
    onChange?: (uiAlert: UiAlert) => void;
    onDelete?: (uiAlert: UiAlert) => void;
}
