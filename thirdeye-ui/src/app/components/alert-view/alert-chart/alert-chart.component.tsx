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
import {
    Box,
    Button,
    ButtonGroup,
    Card,
    CardContent,
    Grid,
    Typography,
} from "@material-ui/core";
import { useQuery } from "@tanstack/react-query";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { getAlertEvaluation } from "../../../rest/alerts/alerts.rest";
import {
    determineTimezoneFromAlertInEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../../utils/alerts/alerts.util";
import {
    createPathWithRecognizedQueryString,
    getAlertsAlertAnomaliesPath,
    getAnomaliesCreatePath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { Pluralize } from "../../pluralize/pluralize.component";
import {
    CHART_SIZE_OPTIONS,
    generateChartOptionsForAlert,
    SMALL_CHART_SIZE,
} from "../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { AlertChartProps } from "./alert-chart.interfaces";

export const AlertChart: FunctionComponent<AlertChartProps> = ({
    anomalies,
    alertId,
    startTime,
    endTime,
}) => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    const getEvaluationQuery = useQuery({
        queryKey: ["evaluation", alertId, startTime, endTime],
        queryFn: () => {
            return getAlertEvaluation({
                alert: { id: alertId },
                start: startTime,
                end: endTime,
            });
        },
    });

    const [expandedChartHeight, setExpandedChartHeight] =
        useState(SMALL_CHART_SIZE);

    const chartDataForExpanded = useMemo(() => {
        if (!getEvaluationQuery.data) {
            return;
        }

        const timezone = determineTimezoneFromAlertInEvaluation(
            getEvaluationQuery.data?.alert.template
        );

        return generateChartOptionsForAlert(
            Object.values(getEvaluationQuery.data?.detectionEvaluations)[0],
            anomalies,
            t,
            navigate,
            timezone,
            shouldHideTimeInDatetimeFormat(
                getEvaluationQuery.data?.alert.template
            )
        );
    }, [getEvaluationQuery.data, anomalies]);

    const handleCreateAlertAnomaly = (): void => {
        // Use the selected start and end time for the anomaly
        const redirectSearchParams = new URLSearchParams([
            [TimeRangeQueryStringKey.START_TIME, `${startTime}`],
            [TimeRangeQueryStringKey.END_TIME, `${endTime}`],
        ] as string[][]);

        // Go to the report anomaly page with new params
        const path = createPathWithRecognizedQueryString(
            getAnomaliesCreatePath(alertId),
            redirectSearchParams
        );
        navigate(path);
    };

    return (
        <Grid item xs={12}>
            <Card variant="outlined">
                <CardContent>
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="flex-end"
                    >
                        <Grid item>
                            <Button
                                color="primary"
                                variant="text"
                                onClick={() => {
                                    handleCreateAlertAnomaly();
                                }}
                            >
                                {t("label.report-missed-anomaly")}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button
                                color="primary"
                                component={RouterLink}
                                disabled={anomalies.length === 0}
                                to={getAlertsAlertAnomaliesPath(alertId)}
                                variant="text"
                            >
                                {anomalies.length > 0 && (
                                    <span>
                                        {t("label.view")}{" "}
                                        <Pluralize
                                            count={anomalies.length}
                                            plural={t("label.anomalies")}
                                            singular={t("label.anomaly")}
                                        />
                                    </span>
                                )}
                                {anomalies.length === 0 && (
                                    <Pluralize
                                        count={anomalies.length}
                                        plural={t("label.anomalies")}
                                        singular={t("label.anomaly")}
                                    />
                                )}
                            </Button>
                        </Grid>
                    </Grid>
                </CardContent>
                <CardContent>
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="flex-end"
                    >
                        <Grid item>{t("label.chart-height")}:</Grid>
                        <Grid item>
                            <Box textAlign="right">
                                <ButtonGroup
                                    color="secondary"
                                    variant="outlined"
                                >
                                    {CHART_SIZE_OPTIONS.map((sizeOption) => (
                                        <Button
                                            color="primary"
                                            disabled={
                                                expandedChartHeight ===
                                                sizeOption[1]
                                            }
                                            key={sizeOption[0]}
                                            onClick={() =>
                                                setExpandedChartHeight(
                                                    sizeOption[1] as number
                                                )
                                            }
                                        >
                                            {sizeOption[0]}
                                        </Button>
                                    ))}
                                </ButtonGroup>
                            </Box>
                        </Grid>
                    </Grid>

                    <LoadingErrorStateSwitch
                        errorState={
                            <Box pb={20} pt={20}>
                                <NoDataIndicator>
                                    <Typography>
                                        {t(
                                            "message.experienced-error-while-fetching-chart-data-try"
                                        )}
                                    </Typography>
                                    <Box pt={3}>
                                        <Button
                                            color="primary"
                                            variant="outlined"
                                            onClick={() =>
                                                getEvaluationQuery.refetch()
                                            }
                                        >
                                            {t("label.reload-chart-data")}
                                        </Button>
                                    </Box>
                                </NoDataIndicator>
                            </Box>
                        }
                        isError={getEvaluationQuery.isError}
                        isLoading={getEvaluationQuery.isLoading}
                        loadingState={
                            <SkeletonV1
                                animation="pulse"
                                height={550}
                                variant="rect"
                            />
                        }
                    >
                        {!!chartDataForExpanded && (
                            <TimeSeriesChart
                                height={expandedChartHeight}
                                {...chartDataForExpanded}
                            />
                        )}
                    </LoadingErrorStateSwitch>
                </CardContent>
            </Card>
        </Grid>
    );
};
