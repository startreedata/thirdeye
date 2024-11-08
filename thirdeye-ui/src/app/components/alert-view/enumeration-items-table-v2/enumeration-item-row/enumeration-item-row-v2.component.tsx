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
import ExpandLessIcon from "@material-ui/icons/ExpandLess";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { DateTime } from "luxon";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useInView } from "react-intersection-observer";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { SkeletonV1 } from "../../../../platform/components";
import { getAlertEvaluation } from "../../../../rest/alerts/alerts.rest";
import { useFetchQuery } from "../../../../rest/hooks/useFetchQuery";
import {
    determineTimezoneFromAlertInEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../../../utils/alerts/alerts.util";
import { generateNameForEnumerationItem } from "../../../../utils/enumeration-items/enumeration-items.util";
import {
    createPathWithRecognizedQueryString,
    getAlertsAlertAnomaliesPath,
    getAnomaliesCreatePath,
} from "../../../../utils/routes/routes.util";
import { AnomalyWizardQueryParams } from "../../../anomalies-create/create-anomaly-wizard/create-anomaly-wizard.utils";
import { NoDataIndicator } from "../../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { Pluralize } from "../../../pluralize/pluralize.component";
import {
    CHART_SIZE_OPTIONS,
    generateChartOptionsForAlert,
    SMALL_CHART_SIZE,
} from "../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeQueryStringKey } from "../../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import { TimeSeriesChartProps } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { EnumerationItemRowV2Props } from "./enumeration-item-row-v2.interfaces";
import { useEnumerationItemRowV2Styles } from "./enumeration-item-row-v2.style";

export const EnumerationItemRowV2: FunctionComponent<EnumerationItemRowV2Props> =
    ({
        anomalies,
        alertId,
        enumerationItem,
        startTime,
        endTime,
        expanded,
        onExpandChange,
    }) => {
        const navigate = useNavigate();
        const { t } = useTranslation();
        const { ref, inView } = useInView({
            triggerOnce: true,
            delay: 150,
        });

        const getEvaluationQuery = useFetchQuery({
            enabled: false,
            queryKey: [
                "evaluation",
                alertId,
                enumerationItem.params,
                startTime,
                endTime,
            ],
            queryFn: () => {
                return getAlertEvaluation(
                    {
                        alert: { id: alertId },
                        start: startTime,
                        end: endTime,
                    },
                    undefined,
                    enumerationItem
                );
            },
        });

        const [expandedChartHeight, setExpandedChartHeight] =
            useState(SMALL_CHART_SIZE);
        const nameForDetectionEvaluation =
            generateNameForEnumerationItem(enumerationItem);
        const [isExpanded, setIsExpanded] = useState(
            expanded.includes(nameForDetectionEvaluation)
        );
        const classes = useEnumerationItemRowV2Styles();

        useEffect(() => {
            setIsExpanded(expanded.includes(nameForDetectionEvaluation));
        }, [expanded]);

        useEffect(() => {
            if (inView) {
                getEvaluationQuery.refetch();
            }
        }, [inView]);

        const handleCreateAlertAnomaly = (): void => {
            // Use the selected start and end time for the anomaly
            const redirectSearchParams = new URLSearchParams([
                [TimeRangeQueryStringKey.START_TIME, `${startTime}`],
                [TimeRangeQueryStringKey.END_TIME, `${endTime}`],
            ] as string[][]);

            // Add the enumeration item ID as a query param if present
            if (enumerationItem.id) {
                redirectSearchParams.set(
                    AnomalyWizardQueryParams.EnumerationItemId,
                    `${enumerationItem.id}`
                );
            }

            // Go to the report anomaly page with new params
            const path = createPathWithRecognizedQueryString(
                getAnomaliesCreatePath(alertId),
                redirectSearchParams
            );
            navigate(path);
        };

        const [chartData, chartDataForExpanded] = useMemo(() => {
            if (!getEvaluationQuery.data) {
                return [];
            }

            const timezone = determineTimezoneFromAlertInEvaluation(
                getEvaluationQuery.data?.alert.template
            );
            const tsData = generateChartOptionsForAlert(
                Object.values(getEvaluationQuery.data?.detectionEvaluations)[0],
                anomalies,
                t,
                navigate,
                timezone,
                shouldHideTimeInDatetimeFormat(
                    getEvaluationQuery.data?.alert.template
                )
            );
            const tsDataForExpanded: TimeSeriesChartProps = {
                ...tsData,
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

            return [tsData, tsDataForExpanded];
        }, [getEvaluationQuery.data, anomalies]);

        return (
            <Card className={classes.card} innerRef={ref} variant="outlined">
                <CardContent>
                    <Grid container alignItems="center">
                        <Grid item sm={3} xs={12}>
                            <Button
                                color="primary"
                                component={RouterLink}
                                disabled={anomalies.length === 0}
                                to={getAlertsAlertAnomaliesPath(
                                    alertId,
                                    enumerationItem.id
                                )}
                                variant="outlined"
                            >
                                {anomalies.length > 0 && (
                                    <span>
                                        {t("label.investigate")}{" "}
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
                        <Grid
                            item
                            className={
                                classes.reportMissedAnomalyButtonContainer
                            }
                            sm={8}
                            xs={12}
                        >
                            <Button
                                color="primary"
                                variant="outlined"
                                onClick={() => {
                                    handleCreateAlertAnomaly();
                                }}
                            >
                                {t("label.log-missed-anomalies")}
                            </Button>
                        </Grid>
                        <Grid item>
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
                                    className={classes.expandChartButton}
                                    component="span"
                                    display="flex"
                                >
                                    {!isExpanded && (
                                        <>
                                            <span>
                                                {t("label.expand-chart")}
                                            </span>
                                            <ExpandMoreIcon />
                                        </>
                                    )}
                                    {isExpanded && (
                                        <>
                                            <span>
                                                {t("label.expand-chart")}
                                            </span>
                                            <ExpandLessIcon />
                                        </>
                                    )}
                                </Box>
                            </Button>
                        </Grid>
                    </Grid>
                    <Grid container alignItems="center">
                        <Grid
                            item
                            {...(isExpanded
                                ? { sm: 8, xs: 12 }
                                : { sm: 8, xs: 12 })}
                        >
                            <Typography
                                className={classes.name}
                                variant="subtitle1"
                            >
                                {nameForDetectionEvaluation}
                            </Typography>
                        </Grid>
                        {!isExpanded && (
                            <Grid item sm={4} xs={12}>
                                <LoadingErrorStateSwitch
                                    errorState={
                                        <NoDataIndicator>
                                            {t(
                                                "message.experienced-an-issue-fetching-chart-data"
                                            )}
                                        </NoDataIndicator>
                                    }
                                    isError={getEvaluationQuery.isError}
                                    isLoading={getEvaluationQuery.isLoading}
                                    loadingState={
                                        <SkeletonV1
                                            animation="pulse"
                                            height={100}
                                            variant="rect"
                                            width={400}
                                        />
                                    }
                                >
                                    {!!chartData && (
                                        <TimeSeriesChart
                                            height={100}
                                            {...chartData}
                                        />
                                    )}
                                </LoadingErrorStateSwitch>
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
                )}
            </Card>
        );
    };
