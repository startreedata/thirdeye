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
import { Box, Button, Link, TableBody, Typography } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useInView } from "react-intersection-observer";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../../rest/alerts/alerts.actions";
import { UiAnomaly } from "../../../../rest/dto/ui-anomaly.interfaces";
import {
    createAlertEvaluation,
    extractDetectionEvaluation,
} from "../../../../utils/alerts/alerts.util";
import { getUiAnomaly } from "../../../../utils/anomalies/anomalies.util";
import {
    getAlertsAlertViewPath,
    getAnomaliesAnomalyViewPath,
} from "../../../../utils/routes/routes.util";
import { DAY_IN_MILLISECONDS } from "../../../../utils/time/time.util";
import {
    generateChartOptions,
    generateChartOptionsForMetricsReport,
} from "../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import { TimeSeriesChartProps } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { AnomalyRowProps } from "./anomaly-row.interfaces";
import { useAnomaliesRowStyles } from "./anomaly-row.styles";

export const AnomalyRow: FunctionComponent<AnomalyRowProps> = ({ anomaly }) => {
    const classes = useAnomaliesRowStyles();
    const { t } = useTranslation();
    const { ref, inView } = useInView({
        triggerOnce: true,
        delay: 150,
        threshold: 1,
    });
    const [isOpen, setIsOpen] = useState(false);
    const [uiAnomaly] = useState<UiAnomaly>(getUiAnomaly(anomaly));
    const [smallChartOptions, setSmallChartOptions] =
        useState<TimeSeriesChartProps | null>(null);
    const [largeChartOptions, setLargeChartOptions] =
        useState<TimeSeriesChartProps | null>(null);
    const {
        evaluation,
        getEvaluation,
        status: evaluationRequestStatus,
    } = useGetEvaluation();

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        if (inView) {
            const start = anomaly.startTime - DAY_IN_MILLISECONDS * 14;
            const end = anomaly.endTime + DAY_IN_MILLISECONDS * 14;
            getEvaluation(
                createAlertEvaluation(anomaly.alert, start, end),
                undefined,
                anomaly.enumerationItem
            );
        }
    }, [inView]);

    useEffect(() => {
        if (!evaluation) {
            return;
        }

        // At this point enumerationItem should be defined
        const detectionEvalForAnomaly =
            extractDetectionEvaluation(evaluation)[0];

        if (!detectionEvalForAnomaly) {
            return;
        }

        setLargeChartOptions(
            generateChartOptions(detectionEvalForAnomaly, anomaly, [], t)
        );

        const options = generateChartOptionsForMetricsReport(
            detectionEvalForAnomaly,
            [anomaly],
            t
        );

        options.zoom = true;
        options.brush = false;
        options.legend = false;
        options.yAxis = {
            enabled: false,
        };
        options.margins = {
            top: 0,
            bottom: 10, // This needs to exist for the x axis
            left: 0,
            right: 0,
        };
        setSmallChartOptions(options);
    }, [evaluation]);

    return (
        <TableBody innerRef={ref}>
            <TableRow className={isOpen ? classes.noBorder : undefined}>
                <TableCell>
                    <Link
                        component={RouterLink}
                        to={getAnomaliesAnomalyViewPath(anomaly.id)}
                    >
                        #{uiAnomaly.id}
                    </Link>
                </TableCell>
                <TableCell>
                    <Link
                        component={RouterLink}
                        to={getAlertsAlertViewPath(anomaly.alert.id)}
                    >
                        {uiAnomaly.alertName}
                    </Link>
                </TableCell>
                <TableCell>{uiAnomaly.metricName}</TableCell>
                <TableCell>{uiAnomaly.startTime}</TableCell>
                <TableCell>{uiAnomaly.endTime}</TableCell>
                <TableCell>
                    <Typography
                        color={
                            uiAnomaly.negativeDeviation ? "error" : undefined
                        }
                        variant="body2"
                    >
                        {uiAnomaly.deviation}
                    </Typography>
                </TableCell>
                <TableCell>
                    {!isOpen &&
                        evaluationRequestStatus === ActionStatus.Working && (
                            <SkeletonV1 animation="pulse" height={100} />
                        )}
                    {!isOpen && smallChartOptions && (
                        <Box minWidth={200}>
                            <TimeSeriesChart
                                height={100}
                                {...smallChartOptions}
                            />
                        </Box>
                    )}
                </TableCell>
                <TableCell>
                    <Button
                        color="primary"
                        variant="text"
                        onClick={() => setIsOpen(!isOpen)}
                    >
                        {isOpen ? t("label.hide-preview") : t("label.preview")}
                    </Button>
                </TableCell>
            </TableRow>
            {isOpen && largeChartOptions && (
                <TableRow>
                    <TableCell colSpan={10}>
                        <Box paddingBottom={3}>
                            <TimeSeriesChart
                                height={400}
                                {...largeChartOptions}
                            />
                        </Box>
                    </TableCell>
                </TableRow>
            )}
        </TableBody>
    );
};
