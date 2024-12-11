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
import { Box, Grid, Link, Typography } from "@material-ui/core";
import AddCircleOutline from "@material-ui/icons/AddCircleOutline";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import {
    getAlertsAllPath,
    getAlertsEasyCreatePath,
} from "../../../utils/routes/routes.util";
import IconLink from "../../icon-link/icon-link.component";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { ActiveAlertsCountProps } from "./active-alerts-count-v2.interfaces";

export const ActiveAlertsCountV2: FunctionComponent<ActiveAlertsCountProps> = ({
    alertsQuery,
}) => {
    const { t } = useTranslation();

    return (
        <Grid
            container
            alignItems="center"
            direction="row"
            justifyContent="space-between"
        >
            <Grid item data-testId="alert-count">
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
                            isError={alertsQuery.isError}
                            isLoading={alertsQuery.isLoading}
                            loadingState={
                                <Box width={100}>
                                    <Typography variant="h6">
                                        <SkeletonV1 animation="pulse" />
                                    </Typography>
                                </Box>
                            }
                        >
                            <Typography variant="h5">
                                {alertsQuery.data &&
                                    alertsQuery.data.filter(
                                        (alert) => alert.active
                                    ).length}
                            </Typography>
                        </LoadingErrorStateSwitch>
                    </Grid>
                    <Grid item>
                        <Link component={RouterLink} to={getAlertsAllPath()}>
                            <Typography>{t("label.active-alerts")}</Typography>
                        </Link>
                    </Grid>
                </Grid>
            </Grid>
            <Grid item data-testId="alert-create">
                <IconLink
                    icon={<AddCircleOutline />}
                    label={t("label.create-entity", {
                        entity: t("label.alert"),
                    })}
                    route={getAlertsEasyCreatePath()}
                    // route={getAlertsCreatePath()}
                />
            </Grid>
        </Grid>
    );
};
