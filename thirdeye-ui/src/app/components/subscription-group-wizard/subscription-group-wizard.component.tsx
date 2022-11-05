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
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridSelectionModelV1,
    DataGridV1,
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../platform/components";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    EmailScheme,
    NotificationSpec,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { getUiSubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { GroupsEditor } from "./groups-editor/groups-editor.component";
import { SubscriptionGroupPropertiesForm } from "./subscription-group-properties-form/subscription-group-properties-form.component";
import {
    generateDefaultSelection,
    validateSubscriptionGroup,
} from "./subscription-group-whizard.utils";
import { SubscriptionGroupWizardProps } from "./subscription-group-wizard.interfaces";

export const SubscriptionGroupWizard: FunctionComponent<
    SubscriptionGroupWizardProps
> = ({ subscriptionGroup, alerts, onCancel, submitBtnLabel, onFinish }) => {
    const [editedSubscriptionGroup, setEditedSubscriptionGroup] =
        useState<SubscriptionGroup>(subscriptionGroup);
    const [selectedAlertsGroup, setSelectedAlertsGroup] = useState<
        DataGridSelectionModelV1<Alert>
    >(generateDefaultSelection(subscriptionGroup, alerts));
    const [isSubscriptionGroupValid, setIsSubscriptionGroupValid] = useState(
        validateSubscriptionGroup(subscriptionGroup)
    );
    const { t } = useTranslation();

    useEffect(() => {
        // In case of input subscription group, alerts need to be configured for included alerts
        // don't carry name
        if (subscriptionGroup) {
            editedSubscriptionGroup.alerts = getUiSubscriptionGroup(
                subscriptionGroup,
                alerts
            ).alerts as Alert[];
        }
    }, []);

    useEffect(() => {
        setIsSubscriptionGroupValid(
            validateSubscriptionGroup(editedSubscriptionGroup)
        );
    }, [editedSubscriptionGroup]);

    const handleSubmitClick = (): void => {
        onFinish && onFinish(editedSubscriptionGroup);
    };

    const handleSubscriptionChange = (
        modifiedSubscriptionGroup: Partial<SubscriptionGroup>
    ): void => {
        // Update subscription group with form inputs
        setEditedSubscriptionGroup((currentlyEdited) => {
            return { ...currentlyEdited, ...modifiedSubscriptionGroup };
        });
    };

    const onSubscriptionGroupEmailsChange = (emails: string[]): void => {
        // Update subscription group with subscribed emails
        setEditedSubscriptionGroup(
            (newSubscriptionGroup): SubscriptionGroup => {
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
            }
        );
    };

    const handleCancel = (): void => {
        onCancel && onCancel();
    };

    const handleSpecsChange = (specs: NotificationSpec[]): void => {
        setEditedSubscriptionGroup((newSubscriptionGroup) => {
            newSubscriptionGroup.specs = specs;

            return { ...newSubscriptionGroup };
        });
    };

    const alertsColumns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.name"),
            minWidth: 150,
            flex: 1,
        },
    ];

    const handleSelectedAlertsChange = (
        updated: DataGridSelectionModelV1<Alert>
    ): void => {
        if (alerts) {
            let newlySelectedAlertsList: Alert[] = [];

            if (updated && updated.rowKeyValueMap) {
                const selectedById = updated.rowKeyValueMap;

                newlySelectedAlertsList = alerts.filter((group) =>
                    selectedById.has(group.id)
                );
            }

            // Update subscription group with subscribed alerts
            setEditedSubscriptionGroup(
                (newSubscriptionGroup): SubscriptionGroup => {
                    newSubscriptionGroup.alerts = newlySelectedAlertsList;

                    return newSubscriptionGroup;
                }
            );
        }
        setSelectedAlertsGroup(updated);
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Typography variant="h5">
                            {t("label.subscription-group-properties")}
                        </Typography>
                        <Typography variant="body2">
                            {t("message.basic-information-about-group")}
                        </Typography>
                        <Box marginTop={3}>
                            <SubscriptionGroupPropertiesForm
                                subscriptionGroup={editedSubscriptionGroup}
                                onChange={handleSubscriptionChange}
                            />
                        </Box>
                    </PageContentsCardV1>
                </Grid>

                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Typography variant="h5">
                            {t("label.channels")}
                        </Typography>
                        <Typography variant="body2">
                            {t("message.channels-subtitle")}
                        </Typography>
                        <Box marginTop={3}>
                            <GroupsEditor
                                subscriptionGroup={editedSubscriptionGroup}
                                onSpecsChange={handleSpecsChange}
                                onSubscriptionGroupEmailsChange={
                                    onSubscriptionGroupEmailsChange
                                }
                            />
                        </Box>
                    </PageContentsCardV1>
                </Grid>

                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Typography variant="h5">
                            {t("label.subscribe-alerts")}
                        </Typography>

                        <Box marginTop={3}>
                            <Grid item lg={6} md={8} sm={12} xs={12}>
                                <Box height={500}>
                                    <DataGridV1<Alert>
                                        hideBorder
                                        // hideToolbar
                                        columns={alertsColumns}
                                        data={alerts}
                                        rowKey="id"
                                        selectionModel={selectedAlertsGroup}
                                        onSelectionChange={
                                            handleSelectedAlertsChange
                                        }
                                    />
                                </Box>
                            </Grid>
                        </Box>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            {/* Controls */}
            <Box textAlign="right" width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Button color="secondary" onClick={handleCancel}>
                                {t("label.cancel")}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button
                                color="primary"
                                disabled={!isSubscriptionGroupValid}
                                onClick={handleSubmitClick}
                            >
                                {submitBtnLabel}
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </>
    );
};
