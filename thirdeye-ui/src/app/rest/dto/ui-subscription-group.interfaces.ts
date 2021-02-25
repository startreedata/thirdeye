import { SubscriptionGroup } from "./subscription-group.interfaces";

export interface UiSubscriptionGroup {
    id: number;
    name: string;
    alerts: UiSubscriptionGroupAlert[];
    emails: string[];
    subscriptionGroup: SubscriptionGroup | null;
}

export interface UiSubscriptionGroupAlert {
    id: number;
    name: string;
}
