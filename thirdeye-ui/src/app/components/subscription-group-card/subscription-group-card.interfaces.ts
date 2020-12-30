import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export interface SubscriptionGroupCardProps {
    subscriptionGroupCardData: SubscriptionGroupCardData;
    searchWords?: string[];
    hideViewDetailsLinks?: boolean;
    onDelete?: (subscriptionGroupCardData: SubscriptionGroupCardData) => void;
}

export interface SubscriptionGroupCardData {
    id: number;
    name: string;
    alerts: SubscriptionGroupAlert[];
    emails: string[];
    subscriptionGroup: SubscriptionGroup | null;
}

export interface SubscriptionGroupAlert {
    id: number;
    name: string;
}
