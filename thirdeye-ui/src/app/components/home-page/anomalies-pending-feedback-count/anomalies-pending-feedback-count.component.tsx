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
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { getAnomaliesAllPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomaliesPendingFeedbackCountProps } from "./anomlies-pending-feedback-count.interfaces";

export const AnomaliesPendingFeedbackCount: FunctionComponent<AnomaliesPendingFeedbackCountProps> =
    ({ anomalyStatsQuery, classes }) => {
        const { t } = useTranslation();

        return (
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                    <Box>{t("label.anomalies-pending-feedback")}</Box>
                    <Box>
                        <Link component={RouterLink} to={getAnomaliesAllPath()}>
                            {t("label.review-entities", {
                                entity: t("label.anomalies"),
                            })}
                        </Link>
                    </Box>
                </Grid>
                <Grid item>
                    <LoadingErrorStateSwitch
                        errorState={
                            <NoDataIndicator
                                className={classNames(classes?.noDataIndicator)}
                            >
                                {t("message.experienced-issues-fetching-data")}
                            </NoDataIndicator>
                        }
                        isError={anomalyStatsQuery.isError}
                        isLoading={anomalyStatsQuery.isLoading}
                        loadingState={
                            <Box
                                className={classNames(classes?.noDataIndicator)}
                                width={100}
                            >
                                <Typography variant="h2">
                                    <SkeletonV1 animation="pulse" />
                                </Typography>
                            </Box>
                        }
                    >
                        <Typography variant="h2">
                            {anomalyStatsQuery.data &&
                                anomalyStatsQuery.data.totalCount -
                                    anomalyStatsQuery.data.countWithFeedback}
                        </Typography>
                    </LoadingErrorStateSwitch>
                </Grid>
            </Grid>
        );
    };
