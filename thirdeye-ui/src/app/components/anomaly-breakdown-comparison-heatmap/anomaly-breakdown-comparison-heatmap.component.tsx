import {
    Card,
    CardContent,
    CardHeader,
    Divider,
    Grid,
    Typography,
} from "@material-ui/core";
import { map } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { AnomalyBreakdown } from "../../rest/dto/rca.interfaces";
import { getAnomalyMetricBreakdown } from "../../rest/rca/rca.rest";
import { formatDateAndTime } from "../../utils/date-time/date-time.util";
import { Treemap } from "../visualizations/treemap/treemap.component";
import { TreemapData } from "../visualizations/treemap/treemap.interfaces";
import {
    AnomalyBreakdownComparisonData,
    AnomalyBreakdownComparisonDataByDimensionColumn,
    AnomalyBreakdownComparisonHeatmapProps,
    SummarizeDataFunctionParams,
    SummaryData,
} from "./anomaly-breakdown-comparison-heatmap.interfaces";
import { DimensionHeatmapTooltip } from "./dimension-heatmap-tooltip/dimension-heatmap-tooltip.component";

const WEEK_IN_MILLISECONDS = 604800000;
const OFFSET_TO_MILLISECONDS = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: 0,
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: WEEK_IN_MILLISECONDS,
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: 2 * WEEK_IN_MILLISECONDS,
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: 3 * WEEK_IN_MILLISECONDS,
};
const OFFSET_TO_HUMAN_READABLE = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: "",
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: "One Week Ago",
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: "Two Weeks Ago",
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: "Three Weeks Ago",
};

function summarizeDimensionValueData(
    dimensionValueData: SummarizeDataFunctionParams
): SummaryData {
    const totalCount = Object.keys(dimensionValueData).reduce(
        (total, dimensionValueKey) =>
            total + dimensionValueData[dimensionValueKey],
        0
    );
    const summarized: SummaryData = {};

    Object.keys(dimensionValueData).forEach((dimension: string) => {
        summarized[dimension] = {
            count: dimensionValueData[dimension],
            percentage: dimensionValueData[dimension] / totalCount,
            totalCount,
        };
    });

    return summarized;
}

function formatTreemapData(
    dimensionData: AnomalyBreakdownComparisonDataByDimensionColumn
): TreemapData<AnomalyBreakdownComparisonData>[] {
    return [
        { id: dimensionData.column, size: 0, parent: null },
        ...map(dimensionData.dimensionComparisonData, (comparisonData, k) => {
            return {
                id: k,
                size: comparisonData.current,
                parent: dimensionData.column,
                extraData: comparisonData,
            };
        }),
    ];
}

export const AnomalyBreakdownComparisonHeatmap: FunctionComponent<AnomalyBreakdownComparisonHeatmapProps> = ({
    anomalyId,
    comparisonOffset = AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO,
    anomaly,
}: AnomalyBreakdownComparisonHeatmapProps) => {
    const [
        anomalyBreakdownCurrent,
        setAnomalyBreakdownCurrent,
    ] = useState<AnomalyBreakdown | null>(null);
    const [
        anomalyBreakdownComparison,
        setAnomalyBreakdownComparison,
    ] = useState<AnomalyBreakdown | null>(null);
    const [breakdownComparisonData, setBreakdownComparisonData] = useState<
        AnomalyBreakdownComparisonDataByDimensionColumn[] | null
    >(null);
    const { t } = useTranslation();

    useEffect(() => {
        if (!anomalyBreakdownCurrent || !anomalyBreakdownComparison) {
            setBreakdownComparisonData(null);

            return;
        }

        const breakdownComparisonDataByDimensionColumn: AnomalyBreakdownComparisonDataByDimensionColumn[] = [];

        Object.keys(anomalyBreakdownCurrent).forEach((dimensionColumnName) => {
            const currentDimensionValuesData = summarizeDimensionValueData(
                anomalyBreakdownCurrent[dimensionColumnName]
            );
            const comparisonDimensionValuesData = summarizeDimensionValueData(
                anomalyBreakdownComparison[dimensionColumnName]
            );
            const dimensionComparisonData: {
                [key: string]: AnomalyBreakdownComparisonData;
            } = {};

            Object.keys(currentDimensionValuesData).forEach(
                (dimension: string) => {
                    dimensionComparisonData[dimension] = {
                        current: currentDimensionValuesData[dimension].count,
                        currentPercentage:
                            currentDimensionValuesData[dimension].percentage,
                        comparison:
                            comparisonDimensionValuesData[dimension].count,
                        comparisonPercentage:
                            comparisonDimensionValuesData[dimension].percentage,
                        percentageDiff:
                            currentDimensionValuesData[dimension].percentage -
                            comparisonDimensionValuesData[dimension].percentage,
                        currentTotalCount:
                            currentDimensionValuesData[dimension].totalCount,
                        comparisonTotalCount:
                            comparisonDimensionValuesData[dimension].totalCount,
                    };
                }
            );

            breakdownComparisonDataByDimensionColumn.push({
                column: dimensionColumnName,
                dimensionComparisonData,
            });
        });
        setBreakdownComparisonData(breakdownComparisonDataByDimensionColumn);
    }, [anomalyBreakdownCurrent, anomalyBreakdownComparison]);

    useEffect(() => {
        setAnomalyBreakdownCurrent(null);
        setAnomalyBreakdownComparison(null);

        getAnomalyMetricBreakdown(anomalyId, {
            timezone: "UTC",
        }).then((anomalyBreakdown) => {
            setAnomalyBreakdownCurrent(anomalyBreakdown);
        });

        getAnomalyMetricBreakdown(anomalyId, {
            timezone: "UTC",
            offset: comparisonOffset,
        }).then((anomalyBreakdown) => {
            setAnomalyBreakdownComparison(anomalyBreakdown);
        });
    }, [anomalyId, comparisonOffset]);

    return (
        <Card variant="outlined">
            <CardHeader
                title="Heatmap of Change in Contribution"
                titleTypographyProps={{ variant: "h5" }}
            />
            {anomaly && (
                <CardContent>
                    <Grid container>
                        <Grid item xs={12}>
                            <Typography variant="h6">
                                Tooltip Reference
                            </Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <div>
                                <strong>
                                    &quot;{t("label.current")}&quot;
                                </strong>{" "}
                                Data Date Range
                            </div>
                            <div>
                                {formatDateAndTime(anomaly.startTime)}
                                <strong> to </strong>
                                {formatDateAndTime(anomaly.endTime)}
                            </div>
                        </Grid>
                        <Grid item xs={6}>
                            <div>
                                <strong>
                                    &quot;{t("label.comparison")}&quot;
                                </strong>
                                <span>
                                    {" "}
                                    Data Date Range (
                                    {OFFSET_TO_HUMAN_READABLE[comparisonOffset]}
                                    )
                                </span>
                            </div>
                            <div>
                                {formatDateAndTime(
                                    anomaly.startTime -
                                        OFFSET_TO_MILLISECONDS[comparisonOffset]
                                )}
                                <strong> to </strong>
                                {formatDateAndTime(
                                    anomaly.endTime -
                                        OFFSET_TO_MILLISECONDS[comparisonOffset]
                                )}
                            </div>
                        </Grid>
                    </Grid>
                </CardContent>
            )}
            <CardContent>
                {breakdownComparisonData &&
                    React.Children.toArray(
                        breakdownComparisonData.map((data) => (
                            <>
                                <Divider />
                                <Treemap<AnomalyBreakdownComparisonData>
                                    name={data.column}
                                    tooltipElement={DimensionHeatmapTooltip}
                                    treemapData={formatTreemapData(data)}
                                />
                            </>
                        ))
                    )}
            </CardContent>
        </Card>
    );
};
