///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import axios from "axios";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";

const BASE_URL_SUBSCRIPTION_GROUPS = "/api/subscription-groups";

export const getSubscriptionGroup = async (
    id: number
): Promise<SubscriptionGroup> => {
    const response = await axios.get(`${BASE_URL_SUBSCRIPTION_GROUPS}/${id}`);

    return response.data;
};

export const getAllSubscriptionGroups = async (): Promise<
    SubscriptionGroup[]
> => {
    const response = await axios.get(BASE_URL_SUBSCRIPTION_GROUPS);

    return response.data;
};

export const createSubscriptionGroup = async (
    subscriptionGroup: SubscriptionGroup
): Promise<SubscriptionGroup> => {
    const response = await axios.post(BASE_URL_SUBSCRIPTION_GROUPS, [
        subscriptionGroup,
    ]);

    return response.data[0];
};

export const createSubscriptionGroups = async (
    subscriptionGroups: SubscriptionGroup[]
): Promise<SubscriptionGroup[]> => {
    const response = await axios.post(
        BASE_URL_SUBSCRIPTION_GROUPS,
        subscriptionGroups
    );

    return response.data;
};

export const updateSubscriptionGroup = async (
    subscriptionGroup: SubscriptionGroup
): Promise<SubscriptionGroup> => {
    const response = await axios.put(BASE_URL_SUBSCRIPTION_GROUPS, [
        subscriptionGroup,
    ]);

    return response.data[0];
};

export const updateSubscriptionGroups = async (
    subscriptionGroups: SubscriptionGroup[]
): Promise<SubscriptionGroup[]> => {
    const response = await axios.put(
        BASE_URL_SUBSCRIPTION_GROUPS,
        subscriptionGroups
    );

    return response.data;
};

export const deleteSubscriptionGroup = async (
    id: number
): Promise<SubscriptionGroup> => {
    const response = await axios.delete(
        `${BASE_URL_SUBSCRIPTION_GROUPS}/${id}`
    );

    return response.data;
};
