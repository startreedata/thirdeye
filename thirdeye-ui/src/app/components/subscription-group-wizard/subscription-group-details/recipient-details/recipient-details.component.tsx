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

import { Card, CardContent, CardHeader, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    EmailScheme,
    NotificationSpec,
    SubscriptionGroup,
} from "../../../../rest/dto/subscription-group.interfaces";
import { GroupsEditor } from "./groups-editor/groups-editor.component";
import { RecipientDetailsProps } from "./recipient-details.interface";

export const RecipientDetails: FunctionComponent<RecipientDetailsProps> = ({
    subscriptionGroup,
    onChange,
}) => {
    const { t } = useTranslation();

    const onSubscriptionGroupEmailsChange = (emails: string[]): void => {
        // Update subscription group with subscribed emails
        onChange((newSubscriptionGroup): SubscriptionGroup => {
            if (newSubscriptionGroup.notificationSchemes.email) {
                // Add to existing email settings
                newSubscriptionGroup.notificationSchemes.email.to = emails;
            } else {
                // Create and add to email settings
                newSubscriptionGroup.notificationSchemes.email = {
                    to: emails,
                } as EmailScheme;
            }

            return newSubscriptionGroup;
        });
    };

    const handleSpecsChange = (specs: NotificationSpec[]): void => {
        onChange((newSubscriptionGroup) => {
            newSubscriptionGroup.specs = specs;

            return { ...newSubscriptionGroup };
        });
    };

    return (
        <Grid item xs={12}>
            <Card variant="outlined">
                <CardHeader title={t("label.recipient-details")} />
                <CardContent>
                    <GroupsEditor
                        subscriptionGroup={subscriptionGroup}
                        onSpecsChange={handleSpecsChange}
                        onSubscriptionGroupEmailsChange={
                            onSubscriptionGroupEmailsChange
                        }
                    />
                </CardContent>
            </Card>
        </Grid>
    );
};
