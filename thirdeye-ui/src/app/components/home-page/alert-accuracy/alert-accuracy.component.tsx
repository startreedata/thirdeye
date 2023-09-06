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
import InfoOutlinedIcon from "@material-ui/icons/InfoOutlined";
import classNames from "classnames";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1, TooltipV1 } from "../../../platform/components";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AlertAccuracyProps } from "./alert-accuracy.interfaces";

export const AlertAccuracy: FunctionComponent<AlertAccuracyProps> = ({
    appAnalyticsQuery,
    classes,
}) => {
    const { t } = useTranslation();

    const precision = useMemo(() => {
        if (!appAnalyticsQuery.data) {
            return null;
        }
        const totalAnomaliesLabeledAsAnomaly =
            appAnalyticsQuery.data.anomalyStats.feedbackStats.ANOMALY +
            appAnalyticsQuery.data.anomalyStats.feedbackStats.ANOMALY_EXPECTED +
            appAnalyticsQuery.data.anomalyStats.feedbackStats.ANOMALY_NEW_TREND;

        // If there are no anomalies with feedback return n/a
        if (appAnalyticsQuery.data.anomalyStats.countWithFeedback === 0) {
            return "n/a";
        }

        return `${Math.round(
            (totalAnomaliesLabeledAsAnomaly /
                appAnalyticsQuery.data.anomalyStats.countWithFeedback) *
                100
        )}%`;
    }, [appAnalyticsQuery.data]);

    return (
        <Grid container alignItems="center" justifyContent="space-between">
            <Grid item>
                <Grid container justifyContent="center">
                    <Grid item>{t("label.accuracy")}</Grid>
                    <Grid item>
                        <TooltipV1
                            delay={0}
                            placement="top"
                            title={t("message.anomaly-precision-tooltip")}
                        >
                            <Typography variant="subtitle1">
                                <InfoOutlinedIcon fontSize="small" />
                            </Typography>
                        </TooltipV1>
                    </Grid>
                </Grid>
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
                    isError={appAnalyticsQuery.isError}
                    isLoading={appAnalyticsQuery.isLoading}
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
                        {appAnalyticsQuery.data && precision !== null && (
                            <span>{precision}</span>
                        )}
                    </Typography>
                </LoadingErrorStateSwitch>
            </Grid>
        </Grid>
    );
};
