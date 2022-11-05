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
                                <>
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
                                </>
                            );
                        })}
                    </tbody>
                </table>
            </Grid>
        </Grid>
    );
};
