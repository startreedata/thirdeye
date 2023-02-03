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
import IconButton from "@material-ui/core/IconButton";
import { KeyboardArrowDown, KeyboardArrowUp } from "@material-ui/icons";
import DeleteIcon from "@material-ui/icons/Delete";
import { DateTime } from "luxon";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { TooltipV1 } from "../../../../../platform/components";
import { generateNameForDetectionResult } from "../../../../../utils/enumeration-items/enumeration-items.util";
import {
    CHART_SIZE_OPTIONS,
    generateChartOptionsForAlert,
    SMALL_CHART_SIZE,
} from "../../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeSeriesChart } from "../../../../visualizations/time-series-chart/time-series-chart.component";
import { EnumerationItemRowProps } from "./enumeration-item-row.interfaces";

export const EnumerationItemRow: FunctionComponent<EnumerationItemRowProps> = ({
    detectionEvaluation,
    anomalies,
    onDeleteClick,
    timezone,
}) => {
    const { t } = useTranslation();
    const [expandedChartHeight, setExpandedChartHeight] =
        useState(SMALL_CHART_SIZE);
    const nameForDetectionEvaluation =
        generateNameForDetectionResult(detectionEvaluation);
    const [isExpanded, setIsExpanded] = useState(false);

    const tsData = generateChartOptionsForAlert(
        detectionEvaluation,
        anomalies,
        t,
        undefined,
        timezone
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
        <Grid item xs={12}>
            <Card variant="outlined">
                <CardContent>
                    <Grid container alignItems="center">
                        <Grid
                            item
                            {...(isExpanded
                                ? { sm: 10, xs: 12 }
                                : { sm: 4, xs: 12 })}
                        >
                            <Typography variant="subtitle1">
                                {nameForDetectionEvaluation}
                            </Typography>
                        </Grid>
                        {!isExpanded && (
                            <Grid item sm={6} xs={12}>
                                <TimeSeriesChart height={100} {...tsData} />
                            </Grid>
                        )}
                        <Grid item sm={1} xs={6}>
                            <Box textAlign="right">
                                <TooltipV1
                                    delay={0}
                                    title={
                                        t(
                                            "message.remove-item-from-configuration"
                                        ) as string
                                    }
                                >
                                    <IconButton
                                        color="primary"
                                        size="small"
                                        onClick={onDeleteClick}
                                    >
                                        <DeleteIcon />
                                    </IconButton>
                                </TooltipV1>
                            </Box>
                        </Grid>
                        <Grid item sm={1} xs={6}>
                            <Box textAlign="right">
                                <IconButton
                                    color="primary"
                                    size="small"
                                    onClick={() => setIsExpanded(!isExpanded)}
                                >
                                    {isExpanded ? (
                                        <KeyboardArrowUp />
                                    ) : (
                                        <KeyboardArrowDown />
                                    )}
                                </IconButton>
                            </Box>
                        </Grid>
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
