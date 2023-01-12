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
import { Divider, Grid, Tab, Tabs } from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { SubscriptionGroups } from "../../alert-wizard-v2/alert-notifications/subscription-groups/subscription-groups.component";
import { CreateSubscriptionGroup } from "./create-subscription-group/create-subscription-group.component";
import { NotificationConfigurationProps } from "./notification-configuration.interfaces";

const SUB_NAV = [
    {
        label: "label.subscribe-to-existing-notification-groups",
    },
    {
        label: "label.create-a-new-notification-group-for-this-alert",
    },
];

export const NotificationConfiguration: FunctionComponent<NotificationConfigurationProps> =
    ({
        alert,
        initiallySelectedSubscriptionGroups,
        onSubscriptionGroupsChange,
        newSubscriptionGroup,
        onNewSubscriptionGroupChange,
    }) => {
        const { t } = useTranslation();

        const [subNavigationSelected, setSubNavigationSelected] = useState(0);

        return (
            <>
                <Grid item xs={12}>
                    <Tabs value={subNavigationSelected}>
                        {SUB_NAV.map((subNavConfig, idx) => {
                            return (
                                <Tab
                                    key={subNavConfig.label}
                                    label={t(subNavConfig.label)}
                                    value={idx}
                                    onClick={() =>
                                        setSubNavigationSelected(idx)
                                    }
                                />
                            );
                        })}
                    </Tabs>
                    <Divider />
                </Grid>

                {subNavigationSelected === 0 && (
                    <Grid item xs={12}>
                        <SubscriptionGroups
                            hideCreateButton
                            alert={alert}
                            initialSubscriptionGroups={
                                initiallySelectedSubscriptionGroups
                            }
                            onSubscriptionGroupsChange={
                                onSubscriptionGroupsChange
                            }
                        />
                    </Grid>
                )}

                {subNavigationSelected === 1 && (
                    <Grid item xs={12}>
                        <CreateSubscriptionGroup
                            subscriptionGroup={newSubscriptionGroup}
                            onNewSubscriptionGroupChange={
                                onNewSubscriptionGroupChange
                            }
                        />
                    </Grid>
                )}
            </>
        );
    };
