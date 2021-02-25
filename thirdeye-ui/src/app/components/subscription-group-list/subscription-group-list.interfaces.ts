import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";

export interface SubscriptionGroupListProps {
    hideSearchBar?: boolean;
    uiSubscriptionGroups: UiSubscriptionGroup[] | null;
    onDelete?: (uiSubscriptionGroup: UiSubscriptionGroup) => void;
}
