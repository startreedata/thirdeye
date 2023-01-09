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
import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { formatDateAndTimeV1 } from "../../../../platform/utils";
import { DataPoint, NormalizedSeries } from "../time-series-chart.interfaces";
import { TooltipPopoverProps } from "./tooltip.interfaces";
import { useTooltipStyles } from "./tooltip.styles";
import { getDataPointsInSeriesForXValue } from "./tooltip.utils";

export const TooltipPopover: FunctionComponent<TooltipPopoverProps> = ({
    series,
    xValue,
    colorScale,
}) => {
    const timeSeriesChartTooltipClasses = useTooltipStyles();
    const dataPointForXValue: [DataPoint, NormalizedSeries][] =
        getDataPointsInSeriesForXValue(series, xValue);

    return (
        <Grid container>
            {/* Time */}
            <Grid item className={timeSeriesChartTooltipClasses.time} xs={12}>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="center"
                    spacing={0}
                >
                    <Grid item>
                        <Typography variant="overline">
                            {formatDateAndTimeV1(xValue)}
                        </Typography>
                    </Grid>
                </Grid>
            </Grid>

            <Grid item xs={12}>
                <table className={timeSeriesChartTooltipClasses.table}>
                    <tbody>
                        {dataPointForXValue.map(([dataPoint, series]) => {
                            const color =
                                series.color === undefined
                                    ? colorScale(series.name as string)
                                    : series.color;

                            return (
                                <React.Fragment key={series.name}>
                                    {series.tooltip.tooltipFormatter !==
                                        undefined &&
                                        series.tooltip.tooltipFormatter(
                                            dataPoint,
                                            series
                                        )}
                                    {series.tooltip.tooltipFormatter ===
                                        undefined && (
                                        <tr key={series.name}>
                                            <td
                                                style={{
                                                    color,
                                                }}
                                            >
                                                {series.name}
                                            </td>
                                            <td
                                                className={
                                                    timeSeriesChartTooltipClasses.valueCell
                                                }
                                            >
                                                {series.tooltip.pointFormatter(
                                                    dataPoint,
                                                    series
                                                )}
                                            </td>
                                        </tr>
                                    )}
                                </React.Fragment>
                            );
                        })}
                    </tbody>
                </table>
            </Grid>
        </Grid>
    );
};
