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

import { Box, CardContent, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    PageContentsCardV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../platform/rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../../rest/dto/detection.interfaces";
import {
    createAlertEvaluation,
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
} from "../../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { PreviewAnomalyChartProps } from "./preview-anomaly-chart.interfaces";

export const PreviewAnomalyChart: FunctionComponent<PreviewAnomalyChartProps> =
    ({ editedAnomaly: anomaly }) => {
        const {
            evaluation,
            getEvaluation,
            errorMessages,
            status: getEvaluationRequestStatus,
        } = useGetEvaluation();

        const [detectionEvaluation, setDetectionEvaluation] =
            useState<DetectionEvaluation | null>(null);
        const [searchParams] = useSearchParams();
        const { t } = useTranslation();
        const { notify } = useNotificationProviderV1();

        useEffect(() => {
            if (!evaluation || !anomaly) {
                return;
            }

            const detectionEvalForAnomaly =
                extractDetectionEvaluation(evaluation)[0];

            // Only filter for the current anomaly
            detectionEvalForAnomaly.anomalies = [anomaly as Anomaly];
            setDetectionEvaluation(detectionEvalForAnomaly);
        }, [evaluation, anomaly.alert.id]);

        useEffect(() => {
            // Fetched alert or time range changed, fetch alert evaluation
            fetchAlertEvaluation();
        }, [searchParams, anomaly.alert.id, anomaly.enumerationItem]);

        useEffect(() => {
            notifyIfErrors(
                getEvaluationRequestStatus,
                errorMessages,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.chart-data"),
                })
            );
        }, [errorMessages, getEvaluationRequestStatus]);

        const fetchAlertEvaluation = (): void => {
            const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
            const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);

            if (!anomaly || !anomaly.alert || !start || !end) {
                setDetectionEvaluation(null);

                return;
            }
            getEvaluation(
                createAlertEvaluation(
                    anomaly.alert,
                    Number(start),
                    Number(end)
                ),
                undefined,
                anomaly.enumerationItem
            );
        };

        /**
         * Chart data will have issues if the evaluation request errors or
         * anomaly belongs to an enumeration item and its request errors
         */
        const chartDataHasIssues =
            getEvaluationRequestStatus === ActionStatus.Error;

        return (
            <Box>
                {chartDataHasIssues && (
                    <PageContentsCardV1>
                        <Box pb={20} pt={20}>
                            <NoDataIndicator />
                        </Box>
                    </PageContentsCardV1>
                )}
                {!chartDataHasIssues && (
                    <AlertEvaluationTimeSeriesCard
                        disableNavigation
                        alertEvaluationTimeSeriesHeight={500}
                        anomalies={[anomaly as Anomaly]}
                        detectionEvaluation={detectionEvaluation}
                        header={
                            <CardContent>
                                <Grid container justifyContent="space-between">
                                    <Grid
                                        item
                                        lg="auto"
                                        md="auto"
                                        sm={4}
                                        xs={12}
                                    >
                                        <Typography variant="h5">
                                            {t("label.preview-entity", {
                                                entity: t("label.anomaly"),
                                            })}
                                        </Typography>
                                        <Typography variant="body2">
                                            Visualize how the anomaly will look
                                            once flagged
                                        </Typography>
                                    </Grid>

                                    <Grid
                                        item
                                        alignContent="flex-end"
                                        lg="auto"
                                        md="auto"
                                        sm={8}
                                        xs={12}
                                    >
                                        <TimeRangeButtonWithContext
                                            timezone={determineTimezoneFromAlertInEvaluation(
                                                evaluation?.alert
                                            )}
                                            onTimeRangeChange={() =>
                                                fetchAlertEvaluation()
                                            }
                                        />
                                        <Typography variant="body2">
                                            Set the date range of the alert
                                            chart below
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </CardContent>
                        }
                        isLoading={
                            getEvaluationRequestStatus === ActionStatus.Working
                        }
                        rootCardProps={{ variant: "elevation" }}
                        timezone={determineTimezoneFromAlertInEvaluation(
                            evaluation?.alert
                        )}
                    />
                )}
            </Box>
        );
    };
