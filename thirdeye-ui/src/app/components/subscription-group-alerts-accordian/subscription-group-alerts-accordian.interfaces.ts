import { Alert } from "../../rest/dto/alert.interfaces";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../entity-cards/subscription-group-card/subscription-group-card.interfaces";

export interface SubscriptionGroupAlertsAccordianProps {
    subscriptionGroupCardData: SubscriptionGroupCardData;
    alerts: Alert[];
    title: string;
    defaultExpanded?: boolean;
    onChange?: (subscriptionGroupAlerts: SubscriptionGroupAlert[]) => void;
}
