import { formatLargeNumberV1 } from "../../../platform/utils";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
import { AnomalyFilterOption } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import {
    Series,
    SeriesType,
    TimeSeriesChartProps,
} from "../../visualizations/time-series-chart/time-series-chart.interfaces";

export const generateSeriesDataForEvaluation = (
    alertEvaluation: AlertEvaluation,
    filteredAlertEvaluations: [AlertEvaluation, AnomalyFilterOption[]][],
    translation: (id: string) => string
): Series[] => {
    const filteredTimeSeriesData: Series[] = filteredAlertEvaluations.map(
        (alertEvalAndFilters) => {
            const [filteredAlertEvaluation, filterOptions] =
                alertEvalAndFilters;
            const filteredAlertEvaluationTimeSeriesData =
                filteredAlertEvaluation.detectionEvaluations
                    .output_AnomalyDetectorResult_0.data;

            return {
                name: filterOptions.map(concatKeyValueWithEqual).join(" & "),
                type: SeriesType.LINE,
                data: filteredAlertEvaluationTimeSeriesData.current
                    .map((value, idx) => {
                        return {
                            y: value,
                            x: filteredAlertEvaluationTimeSeriesData.timestamp[
                                idx
                            ],
                        };
                    })
                    .filter((d) => Number.isFinite(d.y)),
                tooltipValueFormatter: (value: number): string =>
                    formatLargeNumberV1(value),
            };
        }
    );

    const timeSeriesData =
        alertEvaluation.detectionEvaluations.output_AnomalyDetectorResult_0
            .data;

    return [
        {
            name: translation("label.current"),
            type: SeriesType.LINE,
            color: Palette.COLOR_VISUALIZATION_STROKE_CURRENT,
            strokeWidth: Dimension.WIDTH_VISUALIZATION_STROKE_CURRENT,
            data: timeSeriesData.current
                .map((value, idx) => {
                    return {
                        y: value,
                        x: timeSeriesData.timestamp[idx],
                    };
                })
                .filter((d) => Number.isFinite(d.y)),
            tooltipValueFormatter: (value: number): string =>
                formatLargeNumberV1(value),
        },
        {
            name: translation("label.baseline"),
            type: SeriesType.LINE,
            color: Palette.COLOR_VISUALIZATION_STROKE_BASELINE,
            data: timeSeriesData.expected
                .map((value, idx) => {
                    return {
                        y: value,
                        x: timeSeriesData.timestamp[idx],
                    };
                })
                .filter((d) => Number.isFinite(d.y)),
            tooltipValueFormatter: (value: number): string =>
                formatLargeNumberV1(value),
        },
        {
            enabled: false,
            name: translation("label.upper-and-lower-bound"),
            type: SeriesType.AREA_CLOSED,
            color: Palette.COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND,
            data: timeSeriesData.lowerBound.map((value, idx) => {
                return {
                    y: value,
                    y1: timeSeriesData.upperBound[idx],
                    x: timeSeriesData.timestamp[idx],
                };
            }),
            tooltipValueFormatter: (value: number): string =>
                formatLargeNumberV1(value),
        },
        ...filteredTimeSeriesData,
    ];
};

export const generateChartOptions = (
    alertEvaluation: AlertEvaluation,
    anomaly: Anomaly,
    filteredAlertEvaluation: [AlertEvaluation, AnomalyFilterOption[]][],
    translation: (id: string) => string
): TimeSeriesChartProps => {
    return {
        series:
            alertEvaluation !== null
                ? generateSeriesDataForEvaluation(
                      alertEvaluation,
                      filteredAlertEvaluation,
                      translation
                  )
                : [],
        xAxis: {
            plotBands: [
                {
                    start: anomaly.startTime,
                    end: anomaly.endTime,
                    name: translation("label.anomaly-period"),
                    opacity: 0.2,
                    color: Palette.COLOR_VISUALIZATION_STROKE_ANOMALY,
                },
            ],
        },
        legend: true,
        brush: true,
        height: 500,
        tooltip: true,
    };
};
