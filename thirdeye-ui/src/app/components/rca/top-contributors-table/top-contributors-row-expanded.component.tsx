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
import { Box, Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { createAlertEvaluation } from "../../../utils/anomalies/anomalies.util";
import { baselineOffsetToMilliseconds } from "../../../utils/anomaly-breakdown/anomaly-breakdown.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { TimeSeriesChartProps } from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { BaselineAnomalyStatsTable } from "./baseline-anomaly-stats-table/baseline-anomaly-stats-table.component";
import { TopContributorsRowExpandedProps } from "./top-contributors-table.interfaces";
import {
    generateComparisonChartOptions,
    generateFilterStrings,
} from "./top-contributors-table.utils";

export const TopContributorsRowExpanded: FunctionComponent<TopContributorsRowExpandedProps> =
    ({
        row,
        alertId,
        dimensionColumns,
        comparisonOffset,
        timezone,
        hideTime,
        anomaly,
        granularity,
        anomalyDimensionAnalysisData,
    }) => {
        const { notify } = useNotificationProviderV1();
        const { t } = useTranslation();
        const {
            evaluation: nonFilteredEvaluationData,
            getEvaluation: getNonFilteredEvaluation,
            errorMessages: getNonFilteredEvaluationErrorMessages,
            status: getNonFilteredEvaluationStatus,
        } = useGetEvaluation();
        const {
            evaluation: filteredEvaluationData,
            getEvaluation: getFilteredEvaluation,
            errorMessages: getFilteredEvaluationErrorMessages,
            status: getFilteredEvaluationStatus,
        } = useGetEvaluation();

        const [start, end] = useMemo(() => {
            const padding = granularity
                ? baselineOffsetToMilliseconds(granularity) * 7
                : 0;

            return [
                Number(anomaly.startTime) -
                    baselineOffsetToMilliseconds(comparisonOffset) -
                    padding,
                Number(anomaly.endTime) + padding,
            ];
        }, [anomaly]);

        const [chartData, setChartData] = useState<TimeSeriesChartProps | null>(
            null
        );
        const [chartDataIsLoading, setChartDataIsLoading] = useState(true);
        const [chartDataIsHasError, setChartDataIsHasError] = useState(false);

        useEffect(() => {
            setChartData(null);
            fetchAlertEvaluation();
        }, [anomaly, alertId, start, end]);

        useEffect(() => {
            if (
                getFilteredEvaluationStatus !== ActionStatus.Working &&
                getNonFilteredEvaluationStatus !== ActionStatus.Working
            ) {
                if (nonFilteredEvaluationData && filteredEvaluationData) {
                    setChartData(
                        generateComparisonChartOptions(
                            nonFilteredEvaluationData,
                            filteredEvaluationData,
                            comparisonOffset,
                            t,
                            anomaly,
                            timezone,
                            hideTime
                        )
                    );
                }
                setChartDataIsLoading(false);
            } else {
                setChartDataIsLoading(true);
            }

            if (
                getFilteredEvaluationStatus === ActionStatus.Error ||
                getNonFilteredEvaluationStatus === ActionStatus.Error
            ) {
                if (getNonFilteredEvaluationStatus === ActionStatus.Error) {
                    !isEmpty(getNonFilteredEvaluationErrorMessages)
                        ? getNonFilteredEvaluationErrorMessages.map((msg) =>
                              notify(NotificationTypeV1.Error, msg)
                          )
                        : notify(
                              NotificationTypeV1.Error,
                              t("message.error-while-fetching", {
                                  entity: t(
                                      "label.dimension-analysis-row-chart-data"
                                  ),
                              })
                          );
                }
                if (getFilteredEvaluationStatus === ActionStatus.Error) {
                    !isEmpty(getFilteredEvaluationErrorMessages)
                        ? getFilteredEvaluationErrorMessages.map((msg) =>
                              notify(NotificationTypeV1.Error, msg)
                          )
                        : notify(
                              NotificationTypeV1.Error,
                              t("message.error-while-fetching", {
                                  entity: t(
                                      "label.dimension-analysis-row-chart-data"
                                  ),
                              })
                          );
                }
                setChartDataIsHasError(true);
            } else {
                setChartDataIsHasError(false);
            }
        }, [getNonFilteredEvaluationStatus, getFilteredEvaluationStatus]);

        const fetchAlertEvaluation = (): void => {
            const filters: string[] = generateFilterStrings(
                row.names,
                dimensionColumns,
                row.otherDimensionValues
            );

            getNonFilteredEvaluation(
                createAlertEvaluation(alertId, start, end),
                undefined,
                anomaly?.enumerationItem
            );

            getFilteredEvaluation(
                createAlertEvaluation(alertId, start, end),
                filters,
                anomaly?.enumerationItem
            );
        };

        return (
            <>
                <Grid container>
                    <Grid item md={9} sm={7} xs={12}>
                        {chartDataIsLoading && <AppLoadingIndicatorV1 />}
                        {chartDataIsHasError && <NoDataIndicator />}
                        {chartData && <TimeSeriesChart {...chartData} />}
                    </Grid>
                    <Grid item md={3} sm={5} xs={12}>
                        <Box padding="5px">
                            <Grid container>
                                <Grid item xs={12}>
                                    <BaselineAnomalyStatsTable
                                        anomalyDimensionAnalysisData={
                                            anomalyDimensionAnalysisData
                                        }
                                        row={row}
                                    />
                                </Grid>
                            </Grid>
                        </Box>
                    </Grid>
                </Grid>
            </>
        );
    };
