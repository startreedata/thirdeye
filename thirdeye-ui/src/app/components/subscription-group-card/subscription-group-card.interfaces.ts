export interface SubscriptionGroupCardProps {
    subscriptionGroup: SubscriptionGroupCardData;
    searchWords?: string[];
    hideViewDetailsLinks?: boolean;
}

export interface SubscriptionGroupCardData {
    id: number;
    name: string;
    application: string;
    alerts: SubscriptionGroupAlert[];
    emails: string[];
}

export interface SubscriptionGroupAlert {
    id: number;
    name: string;
}
