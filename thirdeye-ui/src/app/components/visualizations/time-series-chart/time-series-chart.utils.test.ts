// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { SeriesType } from "./time-series-chart.interfaces";
import {
    defaultAreaSeriesPointFormatter,
    defaultPointFormatter,
    defaultValueFormatter,
    defaultX1Accessor,
    defaultXAccessor,
    defaultY1Accessor,
    defaultYAccessor,
    getMinMax,
    normalizeSeries,
} from "./time-series-chart.utils";

describe("Time Series Chart Utils", () => {
    it("should return correct min and max values", () => {
        const testSeries = [
            {
                data: [
                    {
                        x: 1,
                        y: 105,
                    },
                    {
                        x: 2,
                        y: 101,
                    },
                    {
                        x: 10,
                        y: 110,
                    },
                    {
                        x: 11,
                        y: 125,
                    },
                    {
                        x: 15,
                        y: 126,
                    },
                ],
            },
            {
                data: [
                    {
                        x: 11,
                        y: 130,
                    },
                    {
                        x: 22,
                        y: 140,
                    },
                    {
                        x: 23,
                        y: 145,
                    },
                    {
                        x: 24,
                        y: 150,
                    },
                    {
                        x: 25,
                        y: 110,
                    },
                ],
            },
        ];

        expect(getMinMax(testSeries)).toEqual([1, 25]);
        expect(getMinMax(testSeries, (d) => d.y)).toEqual([101, 150]);
    });

    it("should set default values for required fields if missing", () => {
        const result = normalizeSeries([
            {
                data: [],
                type: SeriesType.AREA_CLOSED,
                color: "#FFF",
            },
            {
                data: [],
                enabled: false,
                name: "hello world",
            },
        ]);

        expect(result[0]).toEqual({
            data: [],
            enabled: true,
            name: "Series 0",
            type: SeriesType.AREA_CLOSED,
            xAccessor: defaultXAccessor,
            x1Accessor: defaultX1Accessor,
            yAccessor: defaultYAccessor,
            y1Accessor: defaultY1Accessor,
            strokeWidth: 1,
            tooltip: {
                pointFormatter: defaultAreaSeriesPointFormatter,
                valueFormatter: defaultValueFormatter,
            },
            fillOpacity: 1,
            stroke: undefined,
            color: "#FFF",
        });
        expect(result[1]).toEqual({
            data: [],
            enabled: false,
            name: "hello world",
            type: SeriesType.LINE,
            xAccessor: defaultXAccessor,
            x1Accessor: defaultX1Accessor,
            yAccessor: defaultYAccessor,
            y1Accessor: defaultY1Accessor,
            strokeWidth: 1,
            tooltip: {
                pointFormatter: defaultPointFormatter,
                valueFormatter: defaultValueFormatter,
            },
            fillOpacity: 1,
            stroke: undefined,
        });
    });
});
