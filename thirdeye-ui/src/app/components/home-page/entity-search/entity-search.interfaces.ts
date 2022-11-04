import { Alert } from "../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";

export interface EntitySearchProps {
    subscriptionGroups: SubscriptionGroup[] | null;
    alerts: Alert[] | null;
}
