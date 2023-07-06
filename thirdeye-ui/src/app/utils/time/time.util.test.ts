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

import { iso8601ToHuman, iso8601ToMilliseconds } from "./time.util";

describe("Time Util", () => {
    it("iso8601ToMilliseconds return expected milliseconds for duration", () => {
        expect(iso8601ToMilliseconds("P1D")).toEqual(86400000);
        expect(iso8601ToMilliseconds("P7D")).toEqual(86400000 * 7);
        expect(iso8601ToMilliseconds("P1W")).toEqual(86400000 * 7);
        expect(iso8601ToMilliseconds("P30D")).toEqual(86400000 * 30);
        expect(iso8601ToMilliseconds("PT90M")).toEqual(5400000);
    });

    it("iso8601ToHuman return expected string for duration", () => {
        expect(iso8601ToHuman("P1D")).toEqual("1 day");
        expect(iso8601ToHuman("P7D")).toEqual("7 days");
        expect(iso8601ToHuman("P1W")).toEqual("1 week");
        expect(iso8601ToHuman("P30D")).toEqual("30 days");
        expect(iso8601ToHuman("PT90M")).toEqual("90 minutes");
    });
});
