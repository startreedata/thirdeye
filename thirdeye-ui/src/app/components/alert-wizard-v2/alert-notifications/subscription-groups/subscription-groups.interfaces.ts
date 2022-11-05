import { EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../../rest/dto/subscription-group.interfaces";

export interface SubscriptionGroupsProps {
    alert: EditableAlert;
    onSubscriptionGroupsChange: (newGroups: SubscriptionGroup[]) => void;
    initialSubscriptionGroups: SubscriptionGroup[];
}
