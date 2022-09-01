/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { NormalizedSeries } from "../time-series-chart.interfaces";
import { sortSeries } from "./legend.utils";

describe("Legend Util", () => {
    it("sortSeries should return correct order when all have legendIndex", () => {
        const input = [
            {
                name: "1",
                legendIndex: 3,
            },
            {
                name: "2",
                legendIndex: 2,
            },
            {
                name: "3",
                legendIndex: 4,
            },
            {
                name: "4",
                legendIndex: 1,
            },
        ] as NormalizedSeries[];

        expect(sortSeries(input)).toEqual([
            {
                name: "4",
                legendIndex: 1,
            },
            {
                name: "2",
                legendIndex: 2,
            },
            {
                name: "1",
                legendIndex: 3,
            },
            {
                name: "3",
                legendIndex: 4,
            },
        ]);
    });

    it("sortSeries should return correct order when mixed legendIndex maintaining initial sort order", () => {
        const input = [
            {
                name: "1",
                legendIndex: 3,
            },
            {
                name: "2",
            },
            {
                name: "3",
                legendIndex: 4,
            },
            {
                name: "4",
            },
        ] as NormalizedSeries[];

        expect(sortSeries(input)).toEqual([
            {
                name: "2",
            },
            {
                name: "4",
            },
            {
                name: "1",
                legendIndex: 3,
            },
            {
                name: "3",
                legendIndex: 4,
            },
        ]);
    });

    it("sortSeries should maintain sort order when legendIndex does not exist", () => {
        const input = [
            {
                name: "1",
            },
            {
                name: "a2",
            },
            {
                name: "z3",
            },
        ] as NormalizedSeries[];

        expect(sortSeries(input)).toEqual([
            {
                name: "1",
            },
            {
                name: "a2",
            },
            {
                name: "z3",
            },
        ]);
    });
});
