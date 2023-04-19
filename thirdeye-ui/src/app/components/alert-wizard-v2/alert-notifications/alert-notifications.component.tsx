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
import { Box, Button, Grid, Switch, Typography } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { getSubscriptionGroupsCreatePath } from "../../../utils/routes/routes.util";
import { AlertNotificationsProps } from "./alert-notifications.interfaces";
import { SubscriptionGroups } from "./subscription-groups/subscription-groups.component";

export const AlertNotifications: FunctionComponent<AlertNotificationsProps> = ({
    alert,
    onSubscriptionGroupsChange,
    initiallySelectedSubscriptionGroups,
}) => {
    const { t } = useTranslation();
    const [isNotificationsOn, setIsNotificationsOn] = useState(
        !isEmpty(initiallySelectedSubscriptionGroups)
    );

    return (
        <PageContentsCardV1 fullHeight>
            <Grid container>
                <Grid container item xs={12}>
                    <Grid item lg={3} md={5} sm={10} xs={10}>
                        <Box marginBottom={2}>
                            <Typography variant="h5">
                                {t("label.create-notifications")}
                            </Typography>
                            <Typography variant="body2">
                                {t(
                                    "message.setup-notifications-for-your-subscription-groups"
                                )}
                            </Typography>
                        </Box>
                    </Grid>
                    <Grid item lg={9} md={7} sm={2} xs={2}>
                        <Switch
                            checked={isNotificationsOn}
                            color="primary"
                            name="checked"
                            onChange={() =>
                                setIsNotificationsOn(!isNotificationsOn)
                            }
                        />
                    </Grid>
                </Grid>

                {isNotificationsOn && (
                    <SubscriptionGroups
                        alert={alert}
                        emptySubscriptionGroupButton={
                            <Button
                                color="primary"
                                href={getSubscriptionGroupsCreatePath()}
                                target="_blank"
                                variant="outlined"
                            >
                                {t("label.create-entity", {
                                    entity: t("label.subscription-group"),
                                })}
                            </Button>
                        }
                        initialSubscriptionGroups={
                            initiallySelectedSubscriptionGroups
                        }
                        onSubscriptionGroupsChange={onSubscriptionGroupsChange}
                    />
                )}
            </Grid>
        </PageContentsCardV1>
    );
};
