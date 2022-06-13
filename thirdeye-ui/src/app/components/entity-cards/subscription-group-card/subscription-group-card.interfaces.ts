import { UiSubscriptionGroup } from "../../../rest/dto/ui-subscription-group.interfaces";

export interface SubscriptionGroupCardProps {
    uiSubscriptionGroup: UiSubscriptionGroup | null;
    searchWords?: string[];
    showViewDetails?: boolean;
    onDelete?: (uiSubscriptionGroup: UiSubscriptionGroup) => void;
}
