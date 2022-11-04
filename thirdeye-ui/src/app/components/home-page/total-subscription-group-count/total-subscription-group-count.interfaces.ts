import { ActionStatus } from "../../../rest/actions.interfaces";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";

export interface TotalSubscriptionGroupCountProps {
    subscriptionGroups: SubscriptionGroup[] | null;
    getSubscriptionGroupsStatus: ActionStatus;
}
