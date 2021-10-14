import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";

export interface SubscriptionGroupListV1Props {
    subscriptionGroups: UiSubscriptionGroup[] | null;
    onDelete?: (uiSubscriptionGroup: UiSubscriptionGroup) => void;
}
