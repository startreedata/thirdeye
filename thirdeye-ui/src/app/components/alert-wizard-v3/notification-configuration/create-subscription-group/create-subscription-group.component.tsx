/*
 * Copyright 2023 StarTree Inc
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
import { Box, Divider, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationSpec,
    SubscriptionGroup,
} from "../../../../rest/dto/subscription-group.interfaces";
import { PropertiesForm } from "../../../subscription-group-wizard/subscription-group-details/properties-form/properties-form.component";
import { GroupsEditor } from "../../../subscription-group-wizard/subscription-group-details/recipient-details/groups-editor/groups-editor.component";
import { CreateSubscriptionGroupProps } from "./create-subscription-group.interfaces";

export const CreateSubscriptionGroup: FunctionComponent<CreateSubscriptionGroupProps> =
    ({ subscriptionGroup, onNewSubscriptionGroupChange }) => {
        const [editedSubscriptionGroup, setEditedSubscriptionGroup] =
            useState<SubscriptionGroup>(subscriptionGroup);
        const { t } = useTranslation();

        useEffect(() => {
            onNewSubscriptionGroupChange?.(editedSubscriptionGroup);
        }, [editedSubscriptionGroup]);

        const handleSpecsChange = (specs: NotificationSpec[]): void => {
            setEditedSubscriptionGroup((newSubscriptionGroup) => {
                newSubscriptionGroup.specs = specs;

                const final = {
                    ...newSubscriptionGroup,
                };

                return final;
            });
        };

        return (
            <Grid container>
                {editedSubscriptionGroup.specs &&
                    editedSubscriptionGroup.specs.length > 0 && (
                        <Grid item xs={12}>
                            <PropertiesForm
                                customHeader={
                                    <>
                                        <Typography variant="h5">
                                            {t(
                                                "label.create-new-notification-group"
                                            )}
                                        </Typography>
                                        <Typography variant="body2">
                                            {t("message.channels-subtitle")}
                                        </Typography>
                                    </>
                                }
                                values={editedSubscriptionGroup}
                                onChange={setEditedSubscriptionGroup}
                            />
                            <Box paddingTop={3}>
                                <Divider />
                            </Box>
                        </Grid>
                    )}

                <Grid item xs={12}>
                    <GroupsEditor
                        subscriptionGroup={editedSubscriptionGroup}
                        onSpecsChange={handleSpecsChange}
                        // We don't have to worry about legacy email code paths
                        onSubscriptionGroupEmailsChange={() => null}
                    />
                </Grid>
            </Grid>
        );
    };
