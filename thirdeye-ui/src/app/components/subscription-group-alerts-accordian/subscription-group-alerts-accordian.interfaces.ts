import { Alert } from "../../rest/dto/alert.interfaces";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";

export interface SubscriptionGroupAlertsAccordianProps {
    subscriptionGroup: UiSubscriptionGroup | null;
    alerts: Alert[];
    title: string;
    defaultExpanded?: boolean;
    onChange?: (alerts: Alert[]) => void;
}
