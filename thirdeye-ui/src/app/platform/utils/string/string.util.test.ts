///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { parseBooleanV1 } from "./string.util";

describe("String Util", () => {
    it("parseBooleanV1 should return false for invalid string", () => {
        expect(parseBooleanV1(null as unknown as string)).toBeFalsy();
    });

    it("parseBooleanV1 should return false for empty string", () => {
        expect(parseBooleanV1("")).toBeFalsy();
    });

    it("parseBooleanV1 should return false for random string", () => {
        expect(parseBooleanV1("testString")).toBeFalsy();
    });

    it("parseBooleanV1 should return true for 'true'", () => {
        expect(parseBooleanV1("true")).toBeTruthy();
    });

    it("parseBooleanV1 should return true for padded ' true '", () => {
        expect(parseBooleanV1(" true ")).toBeTruthy();
    });
});
