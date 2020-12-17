import { Card, CardContent, CardHeader } from "@material-ui/core";
import ParentSize from "@visx/responsive/lib/components/ParentSize";
import React, { FunctionComponent, ReactElement } from "react";
import {
    getAnomaliesFromAlertEvalution,
    getMargins,
    getTimeSeriesFromAlertEvalution,
} from "../../utils/chart/chart-util";
import { LoadingIndicator } from "../loading-indicator/loading-indicator.component";
import { TimeSeriesChart } from "../timeseries-chart/timeseries-chart.component";
import { TimeSeriesChartCardProps } from "./timeseries-chart-card.interfaces";

export const TimeSeriesChartCard: FunctionComponent<TimeSeriesChartCardProps> = ({
    data,
    title,
}: TimeSeriesChartCardProps) => {
    return (
        <Card variant="outlined">
            <CardHeader title={title} />
            <CardContent style={{ height: 500 }}>
                {data ? (
                    <ParentSize>
                        {({ width, height }): ReactElement => (
                            <TimeSeriesChart
                                showLegend
                                anomalies={getAnomaliesFromAlertEvalution(data)}
                                data={getTimeSeriesFromAlertEvalution(data)}
                                height={height}
                                margin={getMargins({
                                    showLegend: false,
                                })}
                                width={width}
                            />
                        )}
                    </ParentSize>
                ) : (
                    <LoadingIndicator />
                )}
            </CardContent>
        </Card>
    );
};
