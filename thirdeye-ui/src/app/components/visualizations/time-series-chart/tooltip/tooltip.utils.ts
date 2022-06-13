// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import { localPoint } from "@visx/event";
import { bisector } from "d3-array";
import { ScaleTime } from "d3-scale";
import { MouseEvent } from "react";
import {
    DataPoint,
    LineDataPoint,
    NormalizedSeries,
} from "../time-series-chart.interfaces";

export const TOOLTIP_LINE_COLOR = "#AAAAAA";
/**
 * Find the closest valid x value from the data points of all the series to the
 * x value the mouse is event represents.
 */
export const determineXPointForHover = (
    event: MouseEvent<SVGRectElement>,
    series: NormalizedSeries[],
    dateScale: ScaleTime<number, number, never>,
    marginLeft: number
): [number, { x: number; y: number }] | [null, null] => {
    // Convert the mouse position into x and y points
    const coords = localPoint(event);

    if (!coords) {
        return [null, null];
    }

    // Base on the mouse x position, determine the equivalent timestamp for it
    const xDateValue = dateScale.invert(coords.x - marginLeft);

    if (!xDateValue) {
        return [null, null];
    }

    const timestampForMousePosition = xDateValue.getTime();

    /**
     * For the given equivalent timestamp determine the closest
     * data point in each series
     */
    const bisectDate = bisector<DataPoint, number>((d: DataPoint) => d.x).left;

    const closestDataPointForEachSeries: DataPoint[] = [];

    series.forEach((seriesData) => {
        if (seriesData.enabled) {
            const dataPointIdx = bisectDate(
                seriesData.data,
                timestampForMousePosition,
                1
            );
            const candidate0 = seriesData.data[dataPointIdx - 1];
            const candidate1 = seriesData.data[dataPointIdx];
            let d = candidate0;
            if (candidate1) {
                d =
                    timestampForMousePosition - candidate0.x >
                    candidate1.x - timestampForMousePosition
                        ? candidate1
                        : candidate0;
            }

            if (d) {
                closestDataPointForEachSeries.push(d);
            }
        }
    });

    // Choose the timestamp that's closest to the mouse position timestamp
    let closestSeriesDataTimestampToHoverTimestamp = Number.POSITIVE_INFINITY;
    closestDataPointForEachSeries.forEach((candidateDataPoint) => {
        const candidateTimestampDifference = Math.abs(
            candidateDataPoint.x - timestampForMousePosition
        );
        const reigningChamp = Math.abs(
            closestSeriesDataTimestampToHoverTimestamp -
                timestampForMousePosition
        );

        if (candidateTimestampDifference < reigningChamp) {
            closestSeriesDataTimestampToHoverTimestamp = candidateDataPoint.x;
        }
    });

    return [closestSeriesDataTimestampToHoverTimestamp, coords];
};

/**
 * For any series that has a data point with the given xValue, return them in an
 * array
 *
 * @param series - Find the datapoint that matches the xValue in the `data` property
 * @param xValue - Find the datapoint that whose `x` value matches this
 */
export const getDataPointsInSeriesForXValue = (
    series: NormalizedSeries[],
    xValue: number
): [DataPoint, NormalizedSeries][] => {
    const dataPointsWithSeries: [DataPoint, NormalizedSeries][] = [];

    series.forEach((seriesData) => {
        if (seriesData.enabled) {
            const dataPointForXValue = seriesData.data.find(
                (d) => d.x === xValue || (d as LineDataPoint).x1 === xValue
            );

            if (dataPointForXValue) {
                dataPointsWithSeries.push([dataPointForXValue, seriesData]);
            }
        }
    });

    return dataPointsWithSeries;
};
