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
import {
    baselineOffsetToMilliseconds,
    comparisonOffsetReadableValue,
} from "./anomaly-breakdown.util";

describe("Anomaly Breakdown Util", () => {
    it("comparisonOffsetReadableValue should return expected human string string", () => {
        expect(comparisonOffsetReadableValue("P1D")).toEqual("1 day ago");
        expect(comparisonOffsetReadableValue("P2D")).toEqual("2 days ago");
        expect(comparisonOffsetReadableValue("P4Y")).toEqual("4 years ago");
    });

    it("comparisonOffsetReadableValue should return error string if unparseable", () => {
        expect(comparisonOffsetReadableValue("PD")).toEqual(
            "could not parse offset"
        );
        expect(comparisonOffsetReadableValue("P2L")).toEqual(
            "could not parse offset"
        );
    });

    it("baselineOffsetToMilliseconds should return milliseconds", () => {
        expect(baselineOffsetToMilliseconds("P1D")).toEqual(86400000);
        expect(baselineOffsetToMilliseconds("P2D")).toEqual(172800000);
        expect(baselineOffsetToMilliseconds("P4Y")).toEqual(126230400000);
    });

    it("baselineOffsetToMilliseconds should return 0 if unreadable", () => {
        expect(baselineOffsetToMilliseconds("PD")).toEqual(0);
        expect(baselineOffsetToMilliseconds("P2L")).toEqual(0);
    });
});
