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
import { Box, Divider, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationSpec,
    SubscriptionGroup,
} from "../../../../rest/dto/subscription-group.interfaces";
import { GroupsEditor } from "../../../subscription-group-wizard/groups-editor/groups-editor.component";
import { SubscriptionGroupPropertiesForm } from "../../../subscription-group-wizard/subscription-group-properties-form/subscription-group-properties-form.component";
import { CreateSubscriptionGroupProps } from "./create-subscription-group.interfaces";

export const CreateSubscriptionGroup: FunctionComponent<CreateSubscriptionGroupProps> =
    ({ subscriptionGroup, onNewSubscriptionGroupChange }) => {
        const [editedSubscriptionGroup, setEditedSubscriptionGroup] =
            useState<SubscriptionGroup>(subscriptionGroup);
        const { t } = useTranslation();

        const handleSubscriptionChange = (
            modifiedSubscriptionGroup: Partial<SubscriptionGroup>
        ): void => {
            // Update subscription group with form inputs
            setEditedSubscriptionGroup((currentlyEdited) => {
                const final = {
                    ...currentlyEdited,
                    ...modifiedSubscriptionGroup,
                };

                onNewSubscriptionGroupChange &&
                    onNewSubscriptionGroupChange(final);

                return final;
            });
        };

        const handleSpecsChange = (specs: NotificationSpec[]): void => {
            setEditedSubscriptionGroup((newSubscriptionGroup) => {
                newSubscriptionGroup.specs = specs;

                const final = {
                    ...newSubscriptionGroup,
                };

                onNewSubscriptionGroupChange &&
                    onNewSubscriptionGroupChange(final);

                return final;
            });
        };

        return (
            <Grid container>
                <Grid item xs={12}>
                    <Typography variant="h5">
                        {t("label.create-new-notification-group")}
                    </Typography>
                    <Typography variant="body2">
                        {t("message.channels-subtitle")}
                    </Typography>
                </Grid>
                {editedSubscriptionGroup.specs &&
                    editedSubscriptionGroup.specs.length > 0 && (
                        <Grid item xs={12}>
                            <SubscriptionGroupPropertiesForm
                                subscriptionGroup={editedSubscriptionGroup}
                                onChange={handleSubscriptionChange}
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
