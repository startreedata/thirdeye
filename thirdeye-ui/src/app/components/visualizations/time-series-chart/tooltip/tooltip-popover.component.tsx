import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { formatDateAndTimeV1 } from "../../../../platform/utils";
import { useAlertEvaluationTimeSeriesTooltipStyles } from "../../alert-evaluation-time-series/alert-evaluation-time-series-tooltip/alert-evaluation-time-series-tooltip.styles";
import { DataPoint, Series } from "../time-series-chart.interfaces";
import { TooltipPopoverProps } from "./tooltip.interfaces";
import { useTooltipStyles } from "./tooltip.styles";
import { getDataPointsInSeriesForXValue } from "./tooltip.utils";

export const TooltipPopover: FunctionComponent<TooltipPopoverProps> = ({
    series,
    xValue,
    colorScale,
}) => {
    const alertEvaluationTimeSeriesTooltipClasses =
        useAlertEvaluationTimeSeriesTooltipStyles();
    const timeSeriesChartTooltipClasses = useTooltipStyles();
    const dataPointForXValue: [DataPoint, Series][] =
        getDataPointsInSeriesForXValue(series, xValue);

    return (
        <Grid container>
            {/* Time */}
            <Grid
                item
                className={alertEvaluationTimeSeriesTooltipClasses.time}
                xs={12}
            >
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
                            return (
                                <tr key={series.name}>
                                    <td
                                        style={{
                                            color: colorScale(
                                                series.name as string
                                            ),
                                        }}
                                    >
                                        {series.name}
                                    </td>
                                    <td
                                        className={
                                            timeSeriesChartTooltipClasses.valueCell
                                        }
                                    >
                                        {dataPoint.y}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </Grid>
        </Grid>
    );
};
