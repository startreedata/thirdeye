/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Button, Card, CardContent, Grid, Typography } from "@material-ui/core";
import { DateTime } from "luxon";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { getAlertsAlertAnomaliesPath } from "../../../../utils/routes/routes.util";
import { Pluralize } from "../../../pluralize/pluralize.component";
import { generateChartOptionsForAlert } from "../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import { generateNameForDetectionResult } from "../enumeration-items-table.util";
import { EnumerationItemRowProps } from "./enumeration-item-row.interfaces";

export const EnumerationItemRow: FunctionComponent<EnumerationItemRowProps> = ({
    alertId,
    detectionEvaluation,
    anomalies,
    expanded,
    onExpandChange,
}) => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const nameForDetectionEvaluation =
        generateNameForDetectionResult(detectionEvaluation);
    const [isExpanded, setIsExpanded] = useState(
        expanded.includes(nameForDetectionEvaluation)
    );

    useEffect(() => {
        setIsExpanded(expanded.includes(nameForDetectionEvaluation));
    }, [expanded]);

    const tsData = generateChartOptionsForAlert(
        detectionEvaluation,
        anomalies,
        t,
        navigate
    );
    const tsDataForExpanded = {
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
        bottom: 40, // This needs to exist for the x axis
        left: 0,
        right: 0,
    };
    tsData.xAxis = {
        tickFormatter: (d: string) => {
            return DateTime.fromJSDate(new Date(d)).toFormat("MMM dd");
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
                                ? { sm: 8, xs: 12 }
                                : { sm: 4, xs: 12 })}
                        >
                            <Typography variant="subtitle1">
                                {nameForDetectionEvaluation}
                            </Typography>
                        </Grid>
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
                                {!isExpanded && <span>View Details</span>}
                                {isExpanded && <span>Hide Details</span>}
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
                        <TimeSeriesChart height={400} {...tsDataForExpanded} />
                    </CardContent>
                )}
            </Card>
        </Grid>
    );
};
