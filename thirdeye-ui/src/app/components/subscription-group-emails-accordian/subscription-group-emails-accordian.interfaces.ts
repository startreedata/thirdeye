import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";

export interface SubscriptionGroupEmailsAccordianProps {
    subscriptionGroup: UiSubscriptionGroup | null;
    title: string;
    defaultExpanded?: boolean;
    onChange?: (emails: string[]) => void;
}
