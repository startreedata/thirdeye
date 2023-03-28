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
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1 } from "../../../platform/components";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../../rest/dto/detection.interfaces";
import { extractDetectionEvaluation } from "../../../utils/alerts/alerts.util";
import { getUiAnomaly } from "../../../utils/anomalies/anomalies.util";
import { AnomalyCard } from "../../entity-cards/anomaly-card/anomaly-card.component";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { AlertEvaluationTimeSeriesCard } from "../../visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { PreviewAnomalyChartProps } from "./preview-anomaly-chart.interfaces";

export const PreviewAnomalyChart: FunctionComponent<PreviewAnomalyChartProps> =
    ({
        editableAnomaly,
        timezone,
        isLoading,
        fetchAlertEvaluation,
        evaluation,
        onRangeSelection,
        anomalyDateRangeControl,
    }) => {
        const { t } = useTranslation();

        const uiAnomaly = useMemo(
            () => getUiAnomaly(editableAnomaly as Anomaly),
            [editableAnomaly]
        );

        const detectionEvaluation = useMemo<DetectionEvaluation | null>(() => {
            if (!evaluation || !editableAnomaly) {
                return null;
            }

            return {
                ...extractDetectionEvaluation(evaluation)[0],
                anomalies: [editableAnomaly as Anomaly],
            };
        }, [evaluation, editableAnomaly?.alert.id]);

        return (
            <EmptyStateSwitch
                emptyState={
                    <>
                        <SkeletonV1
                            animation="pulse"
                            height={80}
                            variant="rect"
                        />
                        <Box py={2} />
                        <SkeletonV1
                            animation="pulse"
                            height={400}
                            variant="rect"
                        />
                    </>
                }
                isEmpty={isLoading}
            >
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
                                    <Typography
                                        color="textSecondary"
                                        variant="body2"
                                    >
                                        {t(
                                            "message.visualize-how-the-anomaly-will-look-once-reported"
                                        )}
                                    </Typography>
                                </Grid>

                                <Grid item lg="auto" md="auto" sm={8} xs={12}>
                                    <TimeRangeButtonWithContext
                                        hideQuickExtend
                                        timezone={timezone}
                                        onTimeRangeChange={(start, end) =>
                                            // To avoid latency related to updating query params
                                            fetchAlertEvaluation({
                                                start,
                                                end,
                                            })
                                        }
                                    />
                                    <Typography
                                        align="right"
                                        color="textSecondary"
                                        display="block"
                                        variant="body2"
                                    >
                                        {t(
                                            "message.set-the-date-range-of-the-alert-chart-below"
                                        )}
                                    </Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <AnomalyCard
                                        timezone={timezone}
                                        uiAnomaly={uiAnomaly}
                                    />
                                </Grid>
                                <Grid item xs={12}>
                                    {anomalyDateRangeControl}
                                </Grid>
                            </Grid>
                        </CardContent>
                    }
                    rootCardProps={{ variant: "elevation" }}
                    timeSeriesProps={{
                        chartEvents: {
                            onRangeSelection,
                        },
                    }}
                    timezone={timezone}
                />
            </EmptyStateSwitch>
        );
    };
