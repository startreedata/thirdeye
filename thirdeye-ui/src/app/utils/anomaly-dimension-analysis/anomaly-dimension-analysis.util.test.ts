/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { areFiltersEqual } from "./anomaly-dimension-analysis";

describe("Anomaly Dimension Analysis Util", () => {
    it("areFiltersEqual should return correct output for equal filters", () => {
        // Simple case
        expect(
            areFiltersEqual(
                [{ key: "1", value: "a" }],
                [{ key: "1", value: "a" }]
            )
        ).toBe(true);
        expect(
            areFiltersEqual(
                [
                    { key: "1", value: "a" },
                    { key: "2", value: "c" },
                    { key: "2", value: "b" },
                ],
                [
                    { key: "2", value: "b" },
                    { key: "1", value: "a" },
                    { key: "2", value: "c" },
                ]
            )
        ).toBe(true);
    });

    it("areFiltersEqual should return correct output for unequal filters", () => {
        // Simple case
        expect(
            areFiltersEqual(
                [{ key: "1", value: "a" }],
                [{ key: "1", value: "b" }]
            )
        ).toBe(false);
        expect(
            areFiltersEqual(
                [
                    { key: "1", value: "a" },
                    { key: "2", value: "c" },
                    { key: "2", value: "b" },
                ],
                [
                    { key: "2", value: "b" },
                    { key: "1", value: "d" },
                    { key: "2", value: "c" },
                ]
            )
        ).toBe(false);
    });
});
