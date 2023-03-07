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

import { CardContent, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../../rest/dto/detection.interfaces";
import {
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
} from "../../../utils/alerts/alerts.util";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { AlertEvaluationTimeSeriesCard } from "../../visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { PreviewAnomalyChartProps } from "./preview-anomaly-chart.interfaces";

export const PreviewAnomalyChart: FunctionComponent<PreviewAnomalyChartProps> =
    ({ editableAnomaly, evaluation, isLoading, fetchAlertEvaluation }) => {
        const [detectionEvaluation, setDetectionEvaluation] =
            useState<DetectionEvaluation | null>(null);
        const { t } = useTranslation();

        useEffect(() => {
            if (!evaluation || !editableAnomaly) {
                setDetectionEvaluation(null);

                return;
            }

            const detectionEvalForAnomaly =
                extractDetectionEvaluation(evaluation)[0];

            // Only filter for the current anomaly
            detectionEvalForAnomaly.anomalies = [editableAnomaly as Anomaly];
            setDetectionEvaluation(detectionEvalForAnomaly);
        }, [evaluation, editableAnomaly.alert.id]);

        return (
            <AlertEvaluationTimeSeriesCard
                disableNavigation
                alertEvaluationTimeSeriesHeight={500}
                anomalies={[editableAnomaly as Anomaly]}
                detectionEvaluation={detectionEvaluation}
                header={
                    <CardContent>
                        <Grid container justifyContent="space-between">
                            <Grid item lg="auto" md="auto" sm={4} xs={12}>
                                <Typography variant="h5">
                                    {t("label.preview-entity", {
                                        entity: t("label.anomaly"),
                                    })}
                                </Typography>
                                <Typography variant="body2">
                                    {t(
                                        "message.visualize-how-the-anomaly-will-look-once-flagged"
                                    )}
                                </Typography>
                            </Grid>

                            <Grid item lg="auto" md="auto" sm={8} xs={12}>
                                <TimeRangeButtonWithContext
                                    timezone={determineTimezoneFromAlertInEvaluation(
                                        evaluation?.alert
                                    )}
                                    onTimeRangeChange={() =>
                                        fetchAlertEvaluation()
                                    }
                                />
                                <Typography variant="body2">
                                    {t(
                                        "message.set-the-date-range-of-the-alert-chart-below"
                                    )}
                                </Typography>
                            </Grid>
                        </Grid>
                    </CardContent>
                }
                isLoading={isLoading}
                rootCardProps={{ variant: "elevation" }}
                timezone={determineTimezoneFromAlertInEvaluation(
                    evaluation?.alert
                )}
            />
        );
    };
