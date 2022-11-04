import { useHTTPAction } from "../create-rest-action";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";
import { GetSubscriptionGroups } from "./subscription-groups.interface";
import { getAllSubscriptionGroups } from "./subscription-groups.rest";

export const useGetSubscriptionGroups = (): GetSubscriptionGroups => {
    const { data, makeRequest, status, errorMessages } = useHTTPAction<
        SubscriptionGroup[]
    >(getAllSubscriptionGroups);

    const getSubscriptionGroups = (): Promise<
        SubscriptionGroup[] | undefined
    > => {
        return makeRequest();
    };

    return {
        subscriptionGroups: data,
        getSubscriptionGroups,
        status,
        errorMessages,
    };
};
