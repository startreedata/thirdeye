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
import { generateNameForDetectionResult } from "../../../../utils/enumeration-items/enumeration-items.util";
import {
    createPathWithRecognizedQueryString,
    getAlertsAlertAnomaliesPath,
    getAnomaliesCreatePath,
} from "../../../../utils/routes/routes.util";
import { AnomalyWizardQueryParams } from "../../../anomalies-create/create-anomaly-wizard/create-anomaly-wizard.utils";
import { Pluralize } from "../../../pluralize/pluralize.component";
import {
    CHART_SIZE_OPTIONS,
    generateChartOptionsForAlert,
    SMALL_CHART_SIZE,
} from "../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeQueryStringKey } from "../../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import { TimeSeriesChartProps } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { EnumerationItemRowProps } from "./enumeration-item-row.interfaces";
import { useEnumerationItemRowStyles } from "./enumeration-item-row.style";

export const EnumerationItemRow: FunctionComponent<EnumerationItemRowProps> = ({
    alertId,
    detectionEvaluation,
    anomalies,
    expanded,
    onExpandChange,
    timezone,
}) => {
    const navigate = useNavigate();

    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const [expandedChartHeight, setExpandedChartHeight] =
        useState(SMALL_CHART_SIZE);
    const nameForDetectionEvaluation =
        generateNameForDetectionResult(detectionEvaluation);
    const [isExpanded, setIsExpanded] = useState(
        expanded.includes(nameForDetectionEvaluation)
    );
    const classes = useEnumerationItemRowStyles();

    useEffect(() => {
        setIsExpanded(expanded.includes(nameForDetectionEvaluation));
    }, [expanded]);

    const handleCreateAlertAnomaly = (): void => {
        // Use the selected start and end time for the anomaly

        const alertChartStartTime = Number(
            searchParams.get(TimeRangeQueryStringKey.START_TIME)
        );
        const alertChartEndTime = Number(
            searchParams.get(TimeRangeQueryStringKey.END_TIME)
        );

        const redirectSearchParams = new URLSearchParams([
            [TimeRangeQueryStringKey.START_TIME, `${alertChartStartTime}`],
            [TimeRangeQueryStringKey.END_TIME, `${alertChartEndTime}`],
        ] as string[][]);

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

    const tsData = generateChartOptionsForAlert(
        detectionEvaluation,
        anomalies,
        t,
        navigate,
        timezone
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

    return (
        <Grid item key={nameForDetectionEvaluation} xs={12}>
            <Card variant="outlined">
                <CardContent>
                    <Grid container alignItems="center">
                        <Grid
                            item
                            {...(isExpanded
                                ? { sm: 6, xs: 12 }
                                : { sm: 4, xs: 12 })}
                        >
                            <Typography
                                className={classes.name}
                                variant="subtitle1"
                            >
                                {nameForDetectionEvaluation}
                            </Typography>
                        </Grid>
                        {isExpanded && (
                            <Grid item sm={2} xs={12}>
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
