import { Alert } from "../../rest/dto/alert.interfaces";

export interface AlertCardProps {
    alert: Alert;
    searchWords?: string[];
    onAlertStateToggle?: (alert: Alert) => void;
}
