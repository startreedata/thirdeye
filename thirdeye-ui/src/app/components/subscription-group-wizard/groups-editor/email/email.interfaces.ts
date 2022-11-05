import { SubscriptionGroup } from "../../../../rest/dto/subscription-group.interfaces";

export interface EmailProps {
    subscriptionGroup: SubscriptionGroup;
    onSubscriptionGroupEmailsChange: (emails: string[]) => void;
    onDeleteClick: () => void;
}
