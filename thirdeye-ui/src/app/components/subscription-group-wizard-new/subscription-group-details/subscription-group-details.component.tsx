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

import React, { FunctionComponent } from "react";
import { PropertiesForm } from "./properties-form/properties-form.component";
import { RecipientDetails } from "./recipient-details/recipient-details.component";
import type { SubscriptionGroupDetailsProps } from "./subscription-group-details.interface";

export const SubscriptionGroupDetails: FunctionComponent<SubscriptionGroupDetailsProps> =
    ({ subscriptionGroup, onChange }) => {
        const propertiesFormValues = {
            cron: subscriptionGroup.cron,
            name: subscriptionGroup.name,
        };

        return (
            <>
                <PropertiesForm
                    values={propertiesFormValues}
                    onChange={onChange}
                />
                <RecipientDetails
                    activeChannels={subscriptionGroup.activeChannels}
                />
            </>
        );
    };
