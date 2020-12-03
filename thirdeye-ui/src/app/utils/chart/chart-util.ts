import { bisector, format, timeFormat } from "d3";
import { isEmpty } from "lodash";
import {
    Margin,
    TimeSeriesProps,
} from "../../components/timeseries-chart/timeseries-chart.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";

export const CHART_SEPRATION_HEIGHT = 30;

export const getMargins = ({ showLegend }: { showLegend: boolean }): Margin => {
    return {
        left: 40,
        top: 10,
        bottom: 30,
        right: showLegend ? 150 : 20,
    };
};

export const getGraphDataFromAPIData = (
    data: AlertEvaluation
): TimeSeriesProps[] => {
    if (isEmpty(data)) {
        return [];
    }
    const detectionKeys = Object.keys(data.detectionEvaluations);

    const lineChartData = data.detectionEvaluations[detectionKeys[0]].data;

    return lineChartData.timestamp.map((time, idx) => ({
        timestamp: new Date(time),
        current: lineChartData.current[idx],
        expacted: +lineChartData.expected[idx],
        lowerBound: +lineChartData.lowerBound[idx],
        upperBound: +lineChartData.upperBound[idx],
    }));
};

// accessors
export const getDate = (d: TimeSeriesProps): Date => d.timestamp;
export const getValue = (d: TimeSeriesProps): number =>
    isNaN(d.current) ? 0 : d.current;
export const getBaseline = (d: TimeSeriesProps): number =>
    isNaN(d.expacted) ? 0 : d.expacted;
export const getLowerBound = (d: TimeSeriesProps): number =>
    isNaN(d.lowerBound) ? d.current : d.lowerBound;
export const getUpperBound = (d: TimeSeriesProps): number =>
    isNaN(d.current) ? 0 : d.current;

// Utils functions for axis and scaling
export const formatDateSort = timeFormat("%b %d");
export const formatDateDetailed = timeFormat("%b %d, %H:%M %p");
export const formatDate = (
    date: Date | number | { valueOf(): number },
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    _i: number
): string => formatDateSort(date as Date);

export const formatValue = (d: number | { valueOf(): number }): string =>
    format("~s")(d);
export const bisectDate = bisector<TimeSeriesProps, Date>(
    (d) => new Date(d.timestamp)
).left;
