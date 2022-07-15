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
import { duplicateKeyForArrayQueryParams } from "./axios.util";

describe("Axios Util", () => {
    describe("getDimensionAnalysisForAnomaly", () => {
        it("should duplicate query param keys if array value", () => {
            const result = duplicateKeyForArrayQueryParams({
                foo: ["bar", "baz"],
                hello: ["world"],
                test: "key",
            });

            expect(result).toEqual("foo=bar&foo=baz&hello=world&test=key");
        });

        it("should return empty string if object is empty", () => {
            const result = duplicateKeyForArrayQueryParams({});

            expect(result).toEqual("");
        });
    });
});
