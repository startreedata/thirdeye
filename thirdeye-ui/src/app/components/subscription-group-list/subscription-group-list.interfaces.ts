export interface SubscriptionGroupListData {
    id: number;
    idText: string;
    name: string;
    alerts: SubscriptionGroupAlert[];
    emails: string[];
}

export interface SubscriptionGroupAlert {
    id: number;
    name: string;
}

export interface SubscriptionGroupListProps {
    subscriptionGroups: SubscriptionGroupListData[];
    onDelete: (subscriptionGroup: SubscriptionGroupListData[]) => void;
}
