import axios, { AxiosResponse } from "axios";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";

const BASE_URL_SUBSCRIPTION_GROUPS = "/api/subscription-groups";

export const getSubscriptionGroup = async (
    id: number
): Promise<SubscriptionGroup> => {
    const response: AxiosResponse = await axios.get(
        `${BASE_URL_SUBSCRIPTION_GROUPS}/${id}`
    );

    return response.data;
};

export const getAllSubscriptionGroups = async (): Promise<
    SubscriptionGroup[]
> => {
    const response: AxiosResponse = await axios.get(
        BASE_URL_SUBSCRIPTION_GROUPS
    );

    return response.data;
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
