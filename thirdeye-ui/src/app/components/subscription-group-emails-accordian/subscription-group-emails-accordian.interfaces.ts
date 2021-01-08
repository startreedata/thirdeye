import { SubscriptionGroupCardData } from "../entity-cards/subscription-group-card/subscription-group-card.interfaces";

export interface SubscriptionGroupEmailsAccordianProps {
    subscriptionGroupCardData: SubscriptionGroupCardData;
    title: string;
    defaultExpanded?: boolean;
    onChange?: (emails: string[]) => void;
}
