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
import { Box, Grid, Typography } from "@material-ui/core";
import SettingsIcon from "@material-ui/icons/Settings";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1 } from "../../../platform/components";
import { getSubscriptionGroupsAllPath } from "../../../utils/routes/routes.util";
import IconLink from "../../icon-link/icon-link.component";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TotalSubscriptionGroupCountV2Props } from "./total-subscription-group-count-v2.interfaces";

export const TotalSubscriptionGroupCountV2: FunctionComponent<TotalSubscriptionGroupCountV2Props> =
    ({ subscriptionGroupsQuery }) => {
        const { t } = useTranslation();

        return (
            <Grid
                container
                alignItems="center"
                direction="row"
                justifyContent="space-between"
            >
                <Grid item>
                    <Grid container alignItems="center" direction="row">
                        <Grid item>
                            <LoadingErrorStateSwitch
                                errorState={
                                    <NoDataIndicator>
                                        {t(
                                            "message.experienced-issues-fetching-data"
                                        )}
                                    </NoDataIndicator>
                                }
                                isError={subscriptionGroupsQuery.isError}
                                isLoading={subscriptionGroupsQuery.isLoading}
                                loadingState={
                                    <Box width={100}>
                                        <Typography variant="h6">
                                            <SkeletonV1 animation="pulse" />
                                        </Typography>
                                    </Box>
                                }
                            >
                                <Typography variant="h5">
                                    {subscriptionGroupsQuery.data &&
                                        subscriptionGroupsQuery.data.length}
                                </Typography>
                            </LoadingErrorStateSwitch>
                        </Grid>
                        <Grid item>
                            <Typography>
                                {t("label.active-entity", {
                                    entity: t("label.groups"),
                                })}
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item>
                    <IconLink
                        icon={<SettingsIcon />}
                        label={t("label.setup-entity", {
                            entity: t("label.notifications"),
                        })}
                        route={getSubscriptionGroupsAllPath()}
                    />
                </Grid>
            </Grid>
        );
    };
