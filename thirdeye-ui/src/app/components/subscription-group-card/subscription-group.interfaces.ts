import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface SubscriptionGroupCardData {
    id: number;
    name: string;
    application: string;
    alerts: SubscriptionGroupAlert[];
    created: number;
    updated: number;
    emails: string[];
    subscriptionGroup: SubscriptionGroup;
}

export interface SubscriptionGroupAlert {
    id: number;
    name: string;
}

export interface SubscriptionGroupCardProps {
    subscriptionGroup: SubscriptionGroupCardData;
    searchWords?: string[];
    hideViewDetailsLinks?: boolean;
    showDetailsExpanded?: boolean;
}
