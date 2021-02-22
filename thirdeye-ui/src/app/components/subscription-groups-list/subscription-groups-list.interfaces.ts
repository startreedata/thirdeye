import { SubscriptionGroupCardData } from "../entity-cards/subscription-group-card/subscription-group-card.interfaces";

export interface SubscriptionGroupsListProps {
    subscriptionGroupCardDatas: SubscriptionGroupCardData[] | null;
    onDelete?: (subscriptionGroupCardData: SubscriptionGroupCardData) => void;
}
