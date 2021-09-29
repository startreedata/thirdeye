import { SubscriptionGroup } from "../../dto/subscription-group.interfaces";
import { ActionHook } from "../actions.interfaces";

export interface FetchSubscriptionGroup extends ActionHook {
    subscriptionGroup: SubscriptionGroup | null;
    fetchSubscriptionGroup: (id: number) => Promise<void>;
}

export interface FetchAllSubscriptionGroups extends ActionHook {
    subscriptionGroups: SubscriptionGroup[] | null;
    fetchAllSubscriptionGroups: () => Promise<void>;
}

export interface CreateSubscriptionGroup extends ActionHook {
    createSubscriptionGroup: (
        subscriptionGroup: SubscriptionGroup
    ) => Promise<void>;
}

export interface CreateSubscriptionGroups extends ActionHook {
    createSubscriptionGroups: (
        subscriptionGroups: SubscriptionGroup[]
    ) => Promise<void>;
}

export interface UpdateSubscriptionGroup extends ActionHook {
    updateSubscriptionGroup: (
        subscriptionGroup: SubscriptionGroup
    ) => Promise<void>;
}

export interface UpdateSubscriptionGroups extends ActionHook {
    updateSubscriptionGroups: (
        subscriptionGroups: SubscriptionGroup[]
    ) => Promise<void>;
}

export interface DeleteSubscriptionGroup extends ActionHook {
    deleteSubscriptionGroup: (id: number) => Promise<void>;
}
