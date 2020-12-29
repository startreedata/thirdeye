import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../subscription-group-card/subscription-group-card.interfaces";

export interface SubscriptionGroupAlertsAccordianProps {
    subscriptionGroup: SubscriptionGroupCardData;
    alerts: SubscriptionGroupAlert[];
    title: string;
    defaultExpanded?: boolean;
    onChange?: (subscriptiongroupAlerts: SubscriptionGroupAlert[]) => void;
}
