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
import { Card, CardContent, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { generateChartOptionsForAlert } from "../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { TimeSeriesChart } from "../time-series-chart/time-series-chart.component";
import { AlertEvaluationTimeSeriesCardProps } from "./alert-evaluation-time-series-card.interfaces";

export const AlertEvaluationTimeSeriesCard: FunctionComponent<AlertEvaluationTimeSeriesCardProps> =
    ({
        detectionEvaluation,
        alertEvaluationTimeSeriesHeight,
        isLoading,
        header,
        onRefresh,
        anomalies,
        disableNavigation,
        timezone,
        rootCardProps,
        timeSeriesProps,
    }) => {
        const navigate = useNavigate();
        const { t } = useTranslation();

        return (
            <LoadingErrorStateSwitch
                isError={false}
                isLoading={!!isLoading}
                loadingState={
                    <PageContentsCardV1>
                        <SkeletonV1
                            animation="pulse"
                            height={alertEvaluationTimeSeriesHeight}
                            variant="rect"
                        />
                    </PageContentsCardV1>
                }
            >
                <Card variant="outlined" {...rootCardProps}>
                    {header && header}
                    {!header && (
                        <CardContent style={{ paddingBottom: 0 }}>
                            <Grid container justifyContent="flex-end">
                                <Grid item>
                                    <TimeRangeButtonWithContext
                                        btnGroupColor="primary"
                                        timezone={timezone}
                                        onTimeRangeChange={(
                                            start: number,
                                            end: number
                                        ) => onRefresh && onRefresh(start, end)}
                                    />
                                </Grid>
                            </Grid>
                        </CardContent>
                    )}
                    <CardContent>
                        {detectionEvaluation && (
                            <TimeSeriesChart
                                height={alertEvaluationTimeSeriesHeight}
                                {...generateChartOptionsForAlert(
                                    detectionEvaluation,
                                    anomalies,
                                    t,
                                    disableNavigation ? undefined : navigate,
                                    timezone
                                )}
                                {...timeSeriesProps}
                            />
                        )}
                    </CardContent>
                </Card>
            </LoadingErrorStateSwitch>
        );
    };
