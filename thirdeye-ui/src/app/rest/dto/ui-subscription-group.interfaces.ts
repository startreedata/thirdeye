import { SubscriptionGroup } from "./subscription-group.interfaces";

export interface UiSubscriptionGroup {
    id: number;
    name: string;
    cron: string;
    alerts: UiSubscriptionGroupAlert[];
    alertCount: string;
    emails: string[];
    emailCount: string;
    subscriptionGroup: SubscriptionGroup | null;
}

export interface UiSubscriptionGroupAlert {
    id: number;
    name: string;
}
