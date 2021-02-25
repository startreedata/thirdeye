import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";

export interface SubscriptionGroupEmailsAccordianProps {
    uiSubscriptionGroup: UiSubscriptionGroup;
    title: string;
    defaultExpanded?: boolean;
    onChange?: (emails: string[]) => void;
}
