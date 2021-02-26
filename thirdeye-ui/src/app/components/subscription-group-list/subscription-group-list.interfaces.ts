import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";

export interface SubscriptionGroupListProps {
    hideSearchBar?: boolean;
    subscriptionGroups: UiSubscriptionGroup[] | null;
    onDelete?: (uiSubscriptionGroup: UiSubscriptionGroup) => void;
}
