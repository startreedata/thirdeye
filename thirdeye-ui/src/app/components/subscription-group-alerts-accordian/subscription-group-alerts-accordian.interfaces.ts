import { Alert } from "../../rest/dto/alert.interfaces";
import {
    UiSubscriptionGroup,
    UiSubscriptionGroupAlert,
} from "../../rest/dto/ui-subscription-group.interfaces";

export interface UiSubscriptionGroupAlertsAccordianProps {
    uiSubscriptionGroup: UiSubscriptionGroup;
    alerts: Alert[];
    title: string;
    defaultExpanded?: boolean;
    onChange?: (uiSubscriptionGroupAlerts: UiSubscriptionGroupAlert[]) => void;
}
