import { Card, CardContent, CardHeader } from "@material-ui/core";
import ParentSize from "@visx/responsive/lib/components/ParentSize";
import React, { FunctionComponent, ReactElement } from "react";
import {
    getGraphDataFromAPIData,
    getMargins,
} from "../../utils/chart/chart-util";
import { PageLoadingIndicator } from "../page-loading-indicator/page-loading-indicator.component";
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
                                data={getGraphDataFromAPIData(data)}
                                height={height}
                                margin={getMargins({
                                    showLegend: false,
                                })}
                                width={width}
                            />
                        )}
                    </ParentSize>
                ) : (
                    <PageLoadingIndicator />
                )}
            </CardContent>
        </Card>
    );
};
