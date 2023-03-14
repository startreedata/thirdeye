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
import {
    Box,
    Button,
    ButtonGroup,
    Card,
    CardContent,
    Grid,
    Typography,
} from "@material-ui/core";
import ExpandLessIcon from "@material-ui/icons/ExpandLess";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { DateTime } from "luxon";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    Link as RouterLink,
    useNavigate,
    useSearchParams,
} from "react-router-dom";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { generateNameForDetectionResult } from "../../../../utils/enumeration-items/enumeration-items.util";
import {
    createPathWithRecognizedQueryString,
    getAlertsAlertAnomaliesPath,
    getAnomaliesCreatePath,
} from "../../../../utils/routes/routes.util";
import { AlertAccuracyColored } from "../../../alert-accuracy-colored/alert-accuracy-colored.component";
import { AnomalyWizardQueryParams } from "../../../anomalies-create/create-anomaly-wizard/create-anomaly-wizard.utils";
import { Pluralize } from "../../../pluralize/pluralize.component";
import {
    CHART_SIZE_OPTIONS,
    generateChartOptionsForAlert,
    SMALL_CHART_SIZE,
} from "../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeQueryStringKey } from "../../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import {
    TimeSeriesChartProps,
    ZoomDomain,
} from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { EnumerationItemRowProps } from "./enumeration-item-row.interfaces";
import { useEnumerationItemRowStyles } from "./enumeration-item-row.style";

export const EnumerationItemRow: FunctionComponent<EnumerationItemRowProps> = ({
    alertId,
    detectionEvaluation,
    anomalies,
    expanded,
    onExpandChange,
    alertStats,
    timezone,
}) => {
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const [expandedChartHeight, setExpandedChartHeight] =
        useState(SMALL_CHART_SIZE);
    const nameForDetectionEvaluation =
        generateNameForDetectionResult(detectionEvaluation);
    const [isExpanded, setIsExpanded] = useState(
        expanded.includes(nameForDetectionEvaluation)
    );
    const [captureDateRangeFromChart, setCaptureDateRangeFromChart] =
        useState(false);
    const classes = useEnumerationItemRowStyles();

    useEffect(() => {
        setIsExpanded(expanded.includes(nameForDetectionEvaluation));
    }, [expanded]);

    const handleCreateAlertAnomaly = ({
        anomalyStartTime,
        anomalyEndTime,
    }: {
        anomalyStartTime: number;
        anomalyEndTime: number;
    }): void => {
        // Use the selected start and end time for the anomaly
        const redirectSearchParams = new URLSearchParams([
            [AnomalyWizardQueryParams.AnomalyStartTime, `${anomalyStartTime}`],
            [AnomalyWizardQueryParams.AnomalyEndTime, `${anomalyEndTime}`],
        ] as string[][]);

        const alertChartStartTime = Number(
            searchParams.get(TimeRangeQueryStringKey.START_TIME)
        );
        const alertChartEndTime = Number(
            searchParams.get(TimeRangeQueryStringKey.END_TIME)
        );

        // Use the start and end query params being used by the current alert, if valid
        if (alertChartStartTime && alertChartEndTime) {
            redirectSearchParams.set(
                TimeRangeQueryStringKey.START_TIME,
                `${alertChartStartTime}`
            );
            redirectSearchParams.set(
                TimeRangeQueryStringKey.END_TIME,
                `${alertChartEndTime}`
            );
        }

        // Add the enumeration item ID as a query param if present
        if (detectionEvaluation.enumerationId) {
            redirectSearchParams.set(
                AnomalyWizardQueryParams.EnumerationItemId,
                `${detectionEvaluation.enumerationId}`
            );
        }

        // Go to the report anomaly page with new params
        const path = createPathWithRecognizedQueryString(
            getAnomaliesCreatePath(alertId),
            redirectSearchParams
        );
        navigate(path);
    };

    const handleRangeSelection = (zoomDomain: ZoomDomain | null): boolean => {
        if (!captureDateRangeFromChart) {
            // Proceed with the default zoom action
            return true;
        }

        // Disable the drag-select
        setCaptureDateRangeFromChart(false);
        if (zoomDomain?.x0 && zoomDomain?.x1 && detectionEvaluation) {
            const { timestamp } = detectionEvaluation.data;

            const anomalyStartTime = timestamp.find((t) => t >= zoomDomain.x0);
            const anomalyEndTime = timestamp.find((t) => t >= zoomDomain.x1);

            if (anomalyStartTime && anomalyEndTime) {
                handleCreateAlertAnomaly({
                    anomalyStartTime,
                    anomalyEndTime,
                });

                // Cancel the zoom
                return false;
            }
        }

        notify(
            NotificationTypeV1.Error,
            "Unable to parse date range from the chart. Please try again."
        );

        // Cancel the zoom
        return false;
    };

    const tsData = generateChartOptionsForAlert(
        detectionEvaluation,
        anomalies,
        t,
        navigate,
        timezone
    );
    const tsDataForExpanded: TimeSeriesChartProps = {
        ...tsData,
        chartEvents: {
            onRangeSelection: handleRangeSelection,
        },
    };
    tsData.brush = false;
    tsData.zoom = true;
    tsData.legend = false;
    tsData.yAxis = {
        enabled: false,
    };
    tsData.margins = {
        top: 0,
        bottom: 10, // This needs to exist for the x axis
        left: 0,
        right: 0,
    };
    tsData.xAxis = {
        ...tsData.xAxis,
        tickFormatter: (d: string) => {
            return DateTime.fromJSDate(new Date(d), {
                zone: timezone,
            }).toFormat("MMM dd");
        },
    };

    return (
        <Grid item key={nameForDetectionEvaluation} xs={12}>
            <Card variant="outlined">
                <CardContent>
                    <Grid container alignItems="center">
                        <Grid
                            item
                            {...(isExpanded
                                ? { sm: 4, xs: 12 }
                                : { sm: 2, xs: 12 })}
                        >
                            <Typography
                                className={classes.name}
                                variant="subtitle1"
                            >
                                {nameForDetectionEvaluation}
                            </Typography>
                        </Grid>
                        <Grid item sm={2} xs={12}>
                            <AlertAccuracyColored
                                alertStats={alertStats}
                                defaultSkeletonProps={{
                                    width: 100,
                                    height: 30,
                                }}
                                renderCustomText={({ noAnomalyData }) =>
                                    // Returning a null here will have the
                                    // component render the default string
                                    // The requirement here is to render nothing
                                    // if `noAnomalyData` is true
                                    noAnomalyData ? <>&nbsp;</> : null
                                }
                            />
                        </Grid>
                        {isExpanded && (
                            <Grid item sm={2} xs={12}>
                                <Button
                                    color="primary"
                                    variant="text"
                                    onClick={() => {
                                        setCaptureDateRangeFromChart(
                                            !captureDateRangeFromChart
                                        );
                                    }}
                                >
                                    {captureDateRangeFromChart
                                        ? "Cancel anomaly selection"
                                        : t("label.report-missed-anomaly")}
                                </Button>
                            </Grid>
                        )}
                        <Grid item sm={2} xs={12}>
                            <Button
                                color="primary"
                                variant="text"
                                onClick={() =>
                                    onExpandChange(
                                        !isExpanded,
                                        nameForDetectionEvaluation
                                    )
                                }
                            >
                                <Box
                                    alignItems="center"
                                    component="span"
                                    display="flex"
                                >
                                    {!isExpanded && (
                                        <>
                                            <span>
                                                {t("label.view-details")}
                                            </span>
                                            <ExpandMoreIcon />
                                        </>
                                    )}
                                    {isExpanded && (
                                        <>
                                            <span>
                                                {t("label.hide-details")}
                                            </span>
                                            <ExpandLessIcon />
                                        </>
                                    )}
                                </Box>
                            </Button>
                        </Grid>
                        <Grid item sm={2} xs={12}>
                            <Button
                                color="primary"
                                component={RouterLink}
                                disabled={anomalies.length === 0}
                                to={getAlertsAlertAnomaliesPath(
                                    alertId,
                                    detectionEvaluation.enumerationId
                                )}
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
                        {!isExpanded && (
                            <Grid item sm={4} xs={12}>
                                <TimeSeriesChart height={100} {...tsData} />
                            </Grid>
                        )}
                    </Grid>
                </CardContent>
                {isExpanded && (
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
                                        {CHART_SIZE_OPTIONS.map(
                                            (sizeOption) => (
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
                                            )
                                        )}
                                    </ButtonGroup>
                                </Box>
                            </Grid>
                        </Grid>

                        <TimeSeriesChart
                            height={expandedChartHeight}
                            {...tsDataForExpanded}
                        />
                    </CardContent>
                )}
            </Card>
        </Grid>
    );
};
