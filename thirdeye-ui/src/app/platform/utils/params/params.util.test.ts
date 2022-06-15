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
import {
    clearHashParamV1,
    clearQueryParamV1,
    getHashParamV1,
    getQueryParamV1,
    setHashParamV1,
    setQueryParamV1,
} from "./params.util";

const systemLocation = location;

jest.mock("../history/history.util", () => ({
    historyV1: {
        location: {
            search: "",
            hash: "",
        },
        replace: jest.fn().mockImplementation((locationObject) => {
            location.search = locationObject.search;
            location.hash = locationObject.hash;
        }),
    },
}));

describe("Params Util", () => {
    beforeAll(() => {
        // Manipulate global location object
        Object.defineProperty(window, "location", {
            value: {
                search: "",
                hash: "",
            },
        });
    });

    afterAll(() => {
        // Restore global location object
        Object.defineProperty(window, "location", {
            value: systemLocation,
        });
    });

    it("getQueryParamV1 should return empty string for invalid key", () => {
        location.search = "";

        expect(getQueryParamV1(null as unknown as string)).toEqual("");
    });

    it("getQueryParamV1 should return empty string for empty key", () => {
        location.search = "";

        expect(getQueryParamV1("")).toEqual("");
    });

    it("getQueryParamV1 should return empty string when key is not found in query parameters", () => {
        location.search = "";

        expect(getQueryParamV1("testKey")).toEqual("");
    });

    it("getQueryParamV1 should return appropriate value from query parameters for key", () => {
        location.search = "testKey=testValue";

        expect(getQueryParamV1("testKey")).toEqual("testValue");
    });

    it("setQueryParamV1 should not set query parameter for invalid key", () => {
        location.search = "";
        location.hash = "";
        setQueryParamV1(null as unknown as string, "testValue");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("");
    });

    it("setQueryParamV1 should not set query parameter for empty key", () => {
        location.search = "";
        location.hash = "";
        setQueryParamV1("", "testValue");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("");
    });

    it("setQueryParamV1 should set query parameter for key", () => {
        location.search = "";
        location.hash = "";
        setQueryParamV1("testKey", "testValue");

        expect(location.search).toEqual("testKey=testValue");
        expect(location.hash).toEqual("");
    });

    it("setQueryParamV1 should set query parameter for existing key", () => {
        location.search = "testKey1=testValue1&testKey2=testValue2";
        location.hash = "";
        setQueryParamV1("testKey1", "testValue3");

        expect(location.search).toEqual(
            "testKey1=testValue3&testKey2=testValue2"
        );
        expect(location.hash).toEqual("");
    });

    it("clearQueryParamV1 should not clear query parameter for invalid key", () => {
        location.search = "testKey=testValue";
        location.hash = "";
        clearQueryParamV1(null as unknown as string);

        expect(location.search).toEqual("testKey=testValue");
        expect(location.hash).toEqual("");
    });

    it("clearQueryParamV1 should not clear query parameter for empty key", () => {
        location.search = "testKey=testValue";
        location.hash = "";
        clearQueryParamV1("");

        expect(location.search).toEqual("testKey=testValue");
        expect(location.hash).toEqual("");
    });

    it("clearQueryParamV1 should clear query parameter for key", () => {
        location.search = "testKey=testValue";
        location.hash = "";
        clearQueryParamV1("testKey");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("");
    });

    it("clearQueryParamV1 should clear query parameter for only the given key", () => {
        location.search = "testKey1=testValue1&testKey2=testValue2";
        location.hash = "";
        clearQueryParamV1("testKey1");

        expect(location.search).toEqual("testKey2=testValue2");
        expect(location.hash).toEqual("");
    });

    it("getHashParamV1 should return empty string for invalid key", () => {
        location.hash = "";

        expect(getHashParamV1(null as unknown as string)).toEqual("");
    });

    it("getHashParamV1 should return empty string for empty key", () => {
        location.hash = "";

        expect(getHashParamV1("")).toEqual("");
    });

    it("getHashParamV1 should return empty string when key is not found in hash parameters", () => {
        location.hash = "";

        expect(getHashParamV1("testKey")).toEqual("");
    });

    it("getHashParamV1 should return appropriate value from hash parameters for key", () => {
        location.hash = "#testKey=testValue";

        expect(getHashParamV1("testKey")).toEqual("testValue");
    });

    it("setHashParamV1 should not set hash parameter for invalid key", () => {
        location.search = "";
        location.hash = "";
        setHashParamV1(null as unknown as string, "testValue");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("");
    });

    it("setHashParamV1 should not set hash parameter for empty key", () => {
        location.search = "";
        location.hash = "";
        setHashParamV1("", "testValue");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("");
    });

    it("setHashParamV1 should set hash parameter for key", () => {
        location.search = "";
        location.hash = "";
        setHashParamV1("testKey", "testValue");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("#testKey=testValue");
    });

    it("setHashParamV1 should set hash parameter for existing key", () => {
        location.search = "";
        location.hash = "#testKey1=testValue1&testKey2=testValue2";
        setHashParamV1("testKey1", "testValue3");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual(
            "#testKey1=testValue3&testKey2=testValue2"
        );
    });

    it("clearHashParamV1 should not clear hash parameter for invalid key", () => {
        location.search = "";
        location.hash = "#testKey=testValue";
        clearHashParamV1(null as unknown as string);

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("#testKey=testValue");
    });

    it("clearHashParamV1 should not clear hash parameter for empty key", () => {
        location.search = "";
        location.hash = "#testKey=testValue";
        clearHashParamV1("");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("#testKey=testValue");
    });

    it("clearHashParamV1 should clear hash parameter for key", () => {
        location.search = "";
        location.hash = "#testKey=testValue";
        clearHashParamV1("testKey");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("");
    });

    it("clearHashParamV1 should clear hash parameter for only the given key", () => {
        location.search = "";
        location.hash = "#testKey1=testValue1&testKey2=testValue2";
        clearHashParamV1("testKey1");

        expect(location.search).toEqual("");
        expect(location.hash).toEqual("#testKey2=testValue2");
    });
});
