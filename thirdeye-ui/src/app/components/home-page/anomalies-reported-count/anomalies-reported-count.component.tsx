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
import { Box, Grid, Link, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { getAnomaliesAllPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomaliesReportedCountProps } from "./anomalies-reported-count.interfaces";

export const AnomaliesReportedCount: FunctionComponent<AnomaliesReportedCountProps> =
    ({ appAnalytics, getAppAnalyticsStatus }) => {
        const { t } = useTranslation();

        return (
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                    <Box>{t("label.anomalies-reported")}</Box>
                    <Box>
                        <Link component={RouterLink} to={getAnomaliesAllPath()}>
                            {t("label.view-recent-entities", {
                                entity: t("label.anomalies"),
                            })}
                        </Link>
                    </Box>
                </Grid>
                <Grid item>
                    <LoadingErrorStateSwitch
                        errorState={
                            <NoDataIndicator>
                                {t("message.experienced-issues-fetching-data")}
                            </NoDataIndicator>
                        }
                        isError={getAppAnalyticsStatus === ActionStatus.Error}
                        isLoading={
                            getAppAnalyticsStatus === ActionStatus.Working
                        }
                        loadingState={
                            <Box width={100}>
                                <Typography variant="h2">
                                    <SkeletonV1 animation="pulse" />
                                </Typography>
                            </Box>
                        }
                    >
                        <Typography variant="h2">
                            {appAnalytics &&
                                appAnalytics.anomalyStats.totalCount}
                        </Typography>
                    </LoadingErrorStateSwitch>
                </Grid>
            </Grid>
        );
    };
