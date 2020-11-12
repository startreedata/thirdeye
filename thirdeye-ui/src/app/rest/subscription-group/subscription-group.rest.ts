import axios, { AxiosResponse } from "axios";
import useSWR, { responseInterface } from "swr";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";

const BASE_URL_SUBSCRIPTION_GROUPS = "/api/subscription-groups";

export const useSubscriptionGroup = (
    id: number
): responseInterface<SubscriptionGroup, Error> => {
    return useSWR(`${BASE_URL_SUBSCRIPTION_GROUPS}/${id}`);
};

export const useAllSubscriptionGroups = (): responseInterface<
    SubscriptionGroup[],
    Error
> => {
    return useSWR(BASE_URL_SUBSCRIPTION_GROUPS);
};

export const createSubscriptionGroup = async (
    subscriptionGroup: SubscriptionGroup
): Promise<SubscriptionGroup> => {
    const response: AxiosResponse = await axios.post(
        BASE_URL_SUBSCRIPTION_GROUPS,
        [subscriptionGroup]
    );

    return response.data[0];
};

export const updateSubscriptionGroup = async (
    subscriptionGroup: SubscriptionGroup
): Promise<SubscriptionGroup> => {
    const response: AxiosResponse = await axios.put(
        BASE_URL_SUBSCRIPTION_GROUPS,
        [subscriptionGroup]
    );

    return response.data[0];
};

export const deleteSubscriptionGroup = async (
    id: number
): Promise<SubscriptionGroup> => {
    const response: AxiosResponse = await axios.delete(
        `${BASE_URL_SUBSCRIPTION_GROUPS}/${id}`
    );

    return response.data;
};
