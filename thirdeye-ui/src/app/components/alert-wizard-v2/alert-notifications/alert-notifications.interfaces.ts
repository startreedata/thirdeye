import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";

export interface AlertNotificationsProps {
    alert: EditableAlert;
    onSubscriptionGroupsChange: (newGroups: SubscriptionGroup[]) => void;
}
