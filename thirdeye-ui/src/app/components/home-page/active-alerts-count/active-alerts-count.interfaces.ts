import { ActionStatus } from "../../../rest/actions.interfaces";
import { Alert } from "../../../rest/dto/alert.interfaces";

export interface ActiveAlertsCountProps {
    alerts: Alert[] | null;
    getAlertsStatus: ActionStatus;
}

export interface EntityOption {
    type: string;
    label: string;
    link: string;
}
