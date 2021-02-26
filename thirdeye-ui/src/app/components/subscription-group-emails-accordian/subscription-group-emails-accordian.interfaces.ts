import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";

export interface SubscriptionGroupEmailsAccordianProps {
    uiSubscriptionGroup: UiSubscriptionGroup | null;
    title: string;
    defaultExpanded?: boolean;
    onChange?: (emails: string[]) => void;
}
