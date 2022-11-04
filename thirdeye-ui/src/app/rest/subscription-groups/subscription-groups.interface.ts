import { ActionHook } from "../actions.interfaces";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";

export interface GetSubscriptionGroups extends ActionHook {
    subscriptionGroups: SubscriptionGroup[] | null;
    getSubscriptionGroups: () => Promise<SubscriptionGroup[] | undefined>;
}
