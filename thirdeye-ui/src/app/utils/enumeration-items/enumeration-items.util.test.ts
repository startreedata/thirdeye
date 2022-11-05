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
import { EnumerationItemInEvaluation } from "../../rest/dto/detection.interfaces";
import {
    doesMatch,
    doesMatchString,
    parseSearchString,
} from "./enumeration-items.util";

describe("Enumeration Item Util", () => {
    it("parseSearchString should return correct key and values for search params", () => {
        expect(
            parseSearchString("foo='bar' AND baz=200 AND another=\"one\"")
        ).toEqual({
            foo: "bar",
            baz: 200,
            another: "one",
        });
        expect(parseSearchString("foo  = 'bar' AND baz=   200 AND")).toEqual({
            foo: "bar",
            baz: 200,
        });
        expect(parseSearchString("baz=   200")).toEqual({
            baz: 200,
        });
        expect(parseSearchString("AND bandz=   200")).toEqual({
            bandz: 200,
        });
        expect(parseSearchString("noQuotes=helloworld")).toEqual({
            noQuotes: "helloworld",
        });
    });

    it("doesMatch should return correct boolean if object matches criteria", () => {
        expect(
            doesMatch(
                {
                    foo: "bar",
                    bar: 200,
                    baz: 500,
                },
                {
                    foo: "bar",
                }
            )
        ).toEqual(true);
        expect(
            doesMatch(
                {
                    foo: "bar",
                },
                {
                    foo: "baz",
                }
            )
        ).toEqual(false);
        expect(
            doesMatch(
                {
                    foo: "bar",
                    bar: 200,
                    baz: 500,
                },
                {
                    bar: 200,
                    baz: 500,
                }
            )
        ).toEqual(true);
        expect(
            doesMatch(
                {
                    foo: "bar",
                    bar: 200,
                    baz: 500,
                },
                {
                    bar: 200,
                    baz: "world",
                }
            )
        ).toEqual(false);
    });

    it("doesMatchString should return correct boolean if object matches criteria", () => {
        expect(
            doesMatchString(
                {
                    params: {
                        foo: "bar",
                        bar: 200,
                        baz: 500,
                    },
                    name: "",
                } as EnumerationItemInEvaluation,
                "bar=200"
            )
        ).toEqual(true);
        expect(
            doesMatchString(
                {
                    params: {
                        foo: "bar",
                    },
                    name: "",
                } as EnumerationItemInEvaluation,
                "foo=world"
            )
        ).toEqual(false);
        expect(
            doesMatchString(
                {
                    params: {
                        FOO: "bar",
                        BAR: 200,
                    },
                    name: "",
                } as EnumerationItemInEvaluation,
                "foo=bar AND bar=200"
            )
        ).toEqual(true);
        expect(
            doesMatchString(
                {
                    params: {
                        FOO: "bar",
                        BAR: 200,
                    },
                    name: "",
                } as EnumerationItemInEvaluation,
                "foo='bar' AND bar=200"
            )
        ).toEqual(true);
    });
});
