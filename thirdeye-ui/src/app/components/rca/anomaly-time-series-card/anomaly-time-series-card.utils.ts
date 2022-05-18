import {
    formatDateAndTimeV1,
    formatLargeNumberV1,
} from "../../../platform/utils";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { Event } from "../../../rest/dto/event.interfaces";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
import { AnomalyFilterOption } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import {
    DataPoint,
    Series,
    SeriesType,
    ThresholdDataPoint,
    TimeSeriesChartProps,
    ZoomDomain,
} from "../../visualizations/time-series-chart/time-series-chart.interfaces";

export const ZOOM_START_KEY = "zoomStart";
export const ZOOM_END_KEY = "zoomEnd";

export const generateSeriesDataForEvaluation = (
    alertEvaluation: AlertEvaluation,
    filteredAlertEvaluations: [AlertEvaluation, AnomalyFilterOption[]][],
    events: Event[],
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
        {
            enabled: true,
            name: translation("label.events"),
            type: SeriesType.LINE_STACKED,
            color: Palette.COLOR_VISUALIZATION_STROKE_EVENTS,
            strokeWidth: Dimension.WIDTH_VISUALIZATION_STROKE_BASELINE,
            data: events.map((event) => ({
                x: event.startTime,
                y: 0,
                x1: event.endTime,
                y1: 0,
            })),
            tooltipValueFormatter: (
                _value: number,
                point: DataPoint
            ): string => {
                return `${formatDateAndTimeV1(point.x)} - ${formatDateAndTimeV1(
                    (point as ThresholdDataPoint).x1
                )}`;
            },
        },

        ...filteredTimeSeriesData,
    ];
};

export const generateChartOptions = (
    alertEvaluation: AlertEvaluation,
    anomaly: Anomaly,
    filteredAlertEvaluation: [AlertEvaluation, AnomalyFilterOption[]][],
    events: Event[],
    translation: (id: string) => string
): TimeSeriesChartProps => {
    return {
        series:
            alertEvaluation !== null
                ? generateSeriesDataForEvaluation(
                      alertEvaluation,
                      filteredAlertEvaluation,
                      events,
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
        tooltip: true,
    };
};

export const determineInitialZoom = (
    searchParams: URLSearchParams
): ZoomDomain | undefined => {
    if (searchParams.has(ZOOM_START_KEY) && searchParams.has(ZOOM_END_KEY)) {
        return {
            x0: Number(searchParams.get(ZOOM_START_KEY)),
            x1: Number(searchParams.get(ZOOM_END_KEY)),
        };
    }

    return undefined;
};
