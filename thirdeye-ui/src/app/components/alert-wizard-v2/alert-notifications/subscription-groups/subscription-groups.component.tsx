/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Button, Grid, Typography } from "@material-ui/core";
import {
    DataGridSelectionModelV1,
    DataGridV1,
    SkeletonV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { SubscriptionGroup } from "../../../../rest/dto/subscription-group.interfaces";
import { useGetSubscriptionGroups } from "../../../../rest/subscription-groups/subscription-groups.actions";
import { getSubscriptionGroupsCreatePath } from "../../../../utils/routes/routes.util";
import { SubscriptionGroupsProps } from "./subscription-groups.interfaces";

export const SubscriptionGroups: FunctionComponent<SubscriptionGroupsProps> = ({
    alert,
    onSubscriptionGroupsChange,
}) => {
    const { t } = useTranslation();
    const { subscriptionGroups, getSubscriptionGroups, status } =
        useGetSubscriptionGroups();
    const [selectedSubscriptionGroup, setSelectedSubscriptionGroup] =
        useState<DataGridSelectionModelV1<SubscriptionGroup>>();

    useEffect(() => {
        getSubscriptionGroups().then((fetchedSubscriptionGroups) => {
            const subscribed = fetchedSubscriptionGroups
                ? fetchedSubscriptionGroups.filter((group) => {
                      return group.alerts.some((item) => item.id === alert.id);
                  })
                : [];

            if (subscribed.length > 0 && !selectedSubscriptionGroup) {
                setSelectedSubscriptionGroup({
                    rowKeyValues: subscribed.map(
                        (e: SubscriptionGroup) => e.id
                    ),
                    rowKeyValueMap: new Map(
                        subscribed.map((group) => [group.id, group])
                    ),
                });
            }
        });
    }, []);

    const handleSelectedSubscriptionGroupChange = (
        updated: DataGridSelectionModelV1<SubscriptionGroup>
    ): void => {
        if (subscriptionGroups) {
            if (updated && updated.rowKeyValueMap) {
                const selectedById = updated.rowKeyValueMap;

                onSubscriptionGroupsChange(
                    subscriptionGroups.filter((group) =>
                        selectedById.has(group.id)
                    )
                );
            } else {
                onSubscriptionGroupsChange([]);
            }
        }
        setSelectedSubscriptionGroup(updated);
    };

    const subscriptionGroupColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 150,
            flex: 1,
        },
    ];

    return (
        <Grid container item xs={12}>
            {status === ActionStatus.Working && (
                <Grid item xs={12}>
                    <SkeletonV1 />
                    <SkeletonV1 />
                    <SkeletonV1 />
                    <SkeletonV1 />
                </Grid>
            )}
            <Grid container item xs={12}>
                {status === ActionStatus.Done &&
                    subscriptionGroups &&
                    subscriptionGroups.length === 0 && (
                        <Grid item xs={12}>
                            <Box padding={3} textAlign="center">
                                <Typography variant="h6">
                                    No subscription groups created
                                </Typography>

                                <Typography variant="body2">
                                    Create a subscription group in order to
                                    create notifications for the alert
                                </Typography>

                                <Box marginTop={3}>
                                    <Button
                                        color="primary"
                                        href={getSubscriptionGroupsCreatePath()}
                                        target="_blank"
                                        variant="outlined"
                                    >
                                        Create group
                                    </Button>
                                </Box>
                            </Box>
                        </Grid>
                    )}

                {status === ActionStatus.Done &&
                    subscriptionGroups &&
                    subscriptionGroups.length > 0 && (
                        <>
                            <Grid item lg={3} md={4}>
                                <Typography variant="h6">
                                    Select subscription groups
                                </Typography>
                            </Grid>
                            <Grid item lg={9} md={8}>
                                <Button
                                    color="primary"
                                    href={getSubscriptionGroupsCreatePath()}
                                    target="_blank"
                                    variant="outlined"
                                >
                                    Create group
                                </Button>
                            </Grid>
                            <Grid item lg={5} md={6} xs={12}>
                                <Box height={300}>
                                    <DataGridV1<SubscriptionGroup>
                                        hideBorder
                                        hideToolbar
                                        columns={subscriptionGroupColumns}
                                        data={subscriptionGroups}
                                        rowKey="id"
                                        onSelectionChange={
                                            handleSelectedSubscriptionGroupChange
                                        }
                                    />
                                </Box>
                            </Grid>
                        </>
                    )}
            </Grid>
        </Grid>
    );
};
