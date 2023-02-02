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
import { Grid, Typography } from "@material-ui/core";
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    PageContentsCardV1,
    SkeletonV1,
} from "../../../../platform/components";
import { formatDateAndTimeV1 } from "../../../../platform/utils";
import { useCommonStyles } from "../../../../utils/material-ui/common.styles";
import { timezoneStringShort } from "../../../../utils/time/time.util";
import { NoDataIndicator } from "../../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomalySummaryCardDetail } from "./anomaly-summary-card-deatil.component";
import { AnomalySummaryCardProps } from "./anomaly-summary-card.interfaces";

export const AnomalySummaryCard: FunctionComponent<AnomalySummaryCardProps> = ({
    uiAnomaly,
    isLoading,
    className,
    timezone,
    alert,
}) => {
    const commonClasses = useCommonStyles();
    const { t } = useTranslation();
    let metricName;

    if (alert && alert.templateProperties) {
        if (
            alert.templateProperties.aggregationColumn &&
            alert.templateProperties.aggregationFunction
        ) {
            metricName = `${alert.templateProperties.aggregationFunction}(${alert.templateProperties.aggregationColumn})`;
        } else if (uiAnomaly && uiAnomaly.metricName) {
            metricName = uiAnomaly.metricName;
        }
    }

    return (
        <PageContentsCardV1 className={className}>
            <LoadingErrorStateSwitch
                errorState={<NoDataIndicator />}
                isError={!uiAnomaly}
                isLoading={!!isLoading}
                loadingState={
                    <Typography variant="body1">
                        <SkeletonV1 preventDelay />
                        <SkeletonV1 preventDelay />
                    </Typography>
                }
            >
                {uiAnomaly && (
                    <Grid container spacing={8}>
                        {/* Metric */}
                        <Grid item>
                            <AnomalySummaryCardDetail
                                label={`${t("label.metric")} ${
                                    alert && alert.templateProperties.dataset
                                }`}
                                value={
                                    alert === null
                                        ? "Loading ..."
                                        : metricName
                                        ? metricName
                                        : t("label.no-data-marker")
                                }
                            />
                        </Grid>

                        {/* Current and Predicted */}
                        <Grid item>
                            <Grid container spacing={2}>
                                <Grid item>
                                    <AnomalySummaryCardDetail
                                        label={t("label.current")}
                                        value={uiAnomaly.current}
                                    />
                                </Grid>
                                <Grid item>
                                    <Grid
                                        container
                                        className={
                                            uiAnomaly.negativeDeviation
                                                ? commonClasses.decreased
                                                : commonClasses.increased
                                        }
                                        spacing={0}
                                    >
                                        <Grid item>{uiAnomaly.deviation}</Grid>
                                        <Grid item>
                                            {uiAnomaly.negativeDeviation && (
                                                <ArrowDownwardIcon fontSize="small" />
                                            )}
                                            {!uiAnomaly.negativeDeviation && (
                                                <ArrowUpwardIcon fontSize="small" />
                                            )}
                                        </Grid>
                                    </Grid>
                                </Grid>
                                <Grid item>
                                    <AnomalySummaryCardDetail
                                        label={t("label.baseline")}
                                        value={uiAnomaly.predicted}
                                    />
                                </Grid>
                            </Grid>
                        </Grid>

                        {/* Start */}
                        <Grid item>
                            <AnomalySummaryCardDetail
                                label={
                                    t("label.start") +
                                    ` (${timezoneStringShort(timezone)})`
                                }
                                value={formatDateAndTimeV1(
                                    uiAnomaly.startTimeVal,
                                    timezone
                                )}
                            />
                        </Grid>

                        {/* Duration */}
                        <Grid item>
                            <AnomalySummaryCardDetail
                                label={t("label.duration")}
                                value={uiAnomaly.duration}
                            />
                        </Grid>
                    </Grid>
                )}
            </LoadingErrorStateSwitch>
        </PageContentsCardV1>
    );
};
