import { SubscriptionGroupCardData } from "../subscription-group-card/subscription-group-card.interfaces";

export interface SubscriptionGroupEmailsAccordianProps {
    subscriptionGroupCardData: SubscriptionGroupCardData;
    title: string;
    defaultExpanded?: boolean;
    onChange?: (emails: string[]) => void;
}
