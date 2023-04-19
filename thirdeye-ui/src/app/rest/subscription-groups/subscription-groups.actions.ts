/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import { useHTTPAction } from "../create-rest-action";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";
import {
    CreateSubscriptionGroup,
    GetSubscriptionGroups,
} from "./subscription-groups.interface";
import {
    createSubscriptionGroup,
    getAllSubscriptionGroups,
} from "./subscription-groups.rest";

export const useGetSubscriptionGroups = (): GetSubscriptionGroups => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<SubscriptionGroup[]>(getAllSubscriptionGroups);

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
        resetData,
    };
};

export const useCreateSubscriptionGroup = (): CreateSubscriptionGroup => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<SubscriptionGroup>(createSubscriptionGroup);

    const createNewSubscriptionGroup = (
        newSubscriptionGroup: SubscriptionGroup
    ): Promise<SubscriptionGroup> => {
        return makeRequest(newSubscriptionGroup) as Promise<SubscriptionGroup>;
    };

    return {
        subscriptionGroup: data,
        createNewSubscriptionGroup,
        status,
        errorMessages,
        resetData,
    };
};
