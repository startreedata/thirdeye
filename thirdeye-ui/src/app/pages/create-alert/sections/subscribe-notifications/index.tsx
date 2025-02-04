/*
 * Copyright 2024 StarTree Inc
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
// external
import React, { useState } from "react";
import { Box, Grid, Switch, Typography } from "@material-ui/core";
import { useTranslation } from "react-i18next";

// app components
import { PageContentsCardV1 } from "../../../../platform/components";
import { NotificationConfiguration } from "../../../../components/alert-wizard-v3/notification-configuration/notification-configuration.component";

// styles
import { subscribeNotificationStyles } from "./styles";

// types
import { SETUP_DETAILS_TEST_IDS } from "../../../alerts-create-guided-page/setup-details/setup-details-page.interface";
import { EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../../rest/dto/subscription-group.interfaces";

// state
import { useCreateAlertStore } from "../../hooks/state";

// utils
import { createEmptySubscriptionGroup } from "../../../../utils/subscription-groups/subscription-groups.util";

export const SubscribeNotification = (): JSX.Element => {
    const componentStyles = subscribeNotificationStyles();
    const { t } = useTranslation();
    const {
        workingAlert,
        newSubscriptionGroup,
        setNewSubscriptionGroup,
        selectedExistingSubscriptionGroups,
        setSelectedExistingSubscriptiongroup,
        selectedDetectionAlgorithm,
    } = useCreateAlertStore();

    const [isNotificationsOn, setIsNotificationsOn] = useState(false);

    const onNewSubscriptionGroupChange = (group: SubscriptionGroup): void => {
        setNewSubscriptionGroup(group);
    };

    const handleSubscriptionGroupChange = (
        groups: SubscriptionGroup[]
    ): void => {
        setSelectedExistingSubscriptiongroup(groups);
    };

    if (!selectedDetectionAlgorithm) {
        return <></>;
    }

    return (
        <Grid item xs={12}>
            <PageContentsCardV1
                className={componentStyles.notificationContainer}
            >
                <Grid container>
                    <Grid item lg={3} md={5} sm={10} xs={10}>
                        <Box marginBottom={2}>
                            <Typography variant="h5">
                                {t("label.configure-notifications")}
                            </Typography>
                            <Typography variant="body2">
                                {t(
                                    "message.select-who-to-notify-when-finding-anomalies"
                                )}
                            </Typography>
                        </Box>
                    </Grid>
                    <Grid item lg={9} md={7} sm={2} xs={2}>
                        <Switch
                            checked={isNotificationsOn}
                            color="primary"
                            data-testid={
                                SETUP_DETAILS_TEST_IDS.CONFIGURATION_SWITCH
                            }
                            name="checked"
                            onChange={() =>
                                setIsNotificationsOn(!isNotificationsOn)
                            }
                        />
                    </Grid>

                    {isNotificationsOn && (
                        <NotificationConfiguration
                            alert={workingAlert as EditableAlert}
                            initiallySelectedSubscriptionGroups={
                                selectedExistingSubscriptionGroups || []
                            }
                            newSubscriptionGroup={
                                newSubscriptionGroup ||
                                createEmptySubscriptionGroup()
                            }
                            onNewSubscriptionGroupChange={
                                onNewSubscriptionGroupChange
                            }
                            onSubscriptionGroupsChange={
                                handleSubscriptionGroupChange
                            }
                        />
                    )}
                </Grid>
            </PageContentsCardV1>
        </Grid>
    );
};
