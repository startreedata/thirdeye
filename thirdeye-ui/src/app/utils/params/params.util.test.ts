/*
 * Copyright 2022 StarTree Inc
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
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    deserializeKeyValuePair,
    getAccessTokenFromHashParams,
    getQueryString,
    getRecognizedQuery,
    getSearchFromQueryString,
    getSearchTextFromQueryString,
    getTimeRangeDurationFromQueryString,
    isValidNumberId,
    serializeKeyValuePair,
    useSetQueryParamsUtil,
} from "./params.util";

const systemLocation = location;

jest.mock("react-router-dom", () => ({
    useSearchParams: jest.fn().mockImplementation(() => {
        return [urlParams, mockSetQueryParamFunc];
    }),
}));

describe("Params Util", () => {
    beforeAll(() => {
        // Manipulate global location object
        Object.defineProperty(window, "location", {
            value: {
                hash: "",
                search: "",
            },
        });
    });

    beforeEach(() => {
        urlParams = new URLSearchParams();
        mockSetQueryParamFunc.mockReset();
    });

    afterAll(() => {
        // Restore global location object
        Object.defineProperty(window, "location", { value: systemLocation });
    });

    it("getAccessTokenFromHashParams should return empty string when access token is not found in hash params", () => {
        location.hash = "";

        expect(getAccessTokenFromHashParams()).toEqual("");
    });

    it("getAccessTokenFromHashParams should return appropriate access token from hash params", () => {
        location.hash = "#access_token=testAccessToken";

        expect(getAccessTokenFromHashParams()).toEqual("testAccessToken");
    });

    it("setSearchInQueryString should set appropriate search in query string", () => {
        const setQueryParamsUtils = useSetQueryParamsUtil();
        setQueryParamsUtils.setSearchInQueryString("testSearchValue");

        expect(mockSetQueryParamFunc).toHaveBeenLastCalledWith(
            new URLSearchParams("search=testSearchValue"),
            { replace: true }
        );
    });

    it("getSearchFromQueryString should return empty string when search is not found in query string", () => {
        location.search = "";

        expect(getSearchFromQueryString()).toEqual("");
    });

    it("getSearchFromQueryString should return appropriate search from query string", () => {
        location.search = "search=testSearchValue";

        expect(getSearchFromQueryString()).toEqual("testSearchValue");
    });

    it("setSearchTextInQueryString should set appropriate search text in query string", () => {
        const setQueryParamsUtils = useSetQueryParamsUtil();
        setQueryParamsUtils.setSearchTextInQueryString("testSearchTextValue");

        expect(mockSetQueryParamFunc).toHaveBeenLastCalledWith(
            new URLSearchParams("searchText=testSearchTextValue"),
            { replace: true }
        );
    });

    it("getSearchTextFromQueryString should return empty string when search text is not found in query string", () => {
        location.search = "";

        expect(getSearchTextFromQueryString()).toEqual("");
    });

    it("getSearchTextFromQueryString should return appropriate search text from query string", () => {
        location.search = "searchText=testSearchTextValue";

        expect(getSearchTextFromQueryString()).toEqual("testSearchTextValue");
    });

    it("setTimeRangeDurationInQueryString should not set invalid time range duration in query string", () => {
        const setQueryParamsUtils = useSetQueryParamsUtil();
        setQueryParamsUtils.setTimeRangeDurationInQueryString(
            null as unknown as TimeRangeDuration
        );

        expect(mockSetQueryParamFunc).not.toHaveBeenCalled();
    });

    it("setTimeRangeDurationInQueryString should set appropriate time range duration in query string", () => {
        const setQueryParamsUtils = useSetQueryParamsUtil();
        setQueryParamsUtils.setTimeRangeDurationInQueryString(
            mockTimeRangeDuration
        );

        expect(mockSetQueryParamFunc).toHaveBeenLastCalledWith(
            new URLSearchParams("timeRange=CUSTOM&startTime=1&endTime=2")
        );
    });

    it("getTimeRangeDurationFromQueryString should return null when time range duration is not found in query string", () => {
        location.search = "";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    it("getTimeRangeDurationFromQueryString should return null when time range is not found in query string", () => {
        location.search = "start_time=1&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    it("getTimeRangeDurationFromQueryString should return null when start time is not found in query string", () => {
        location.search = "time_range=CUSTOM&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    it("getTimeRangeDurationFromQueryString should return null when end time is not found in query string", () => {
        location.search = "time_range=CUSTOM&start_time=1";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    it("getTimeRangeDurationFromQueryString should return null for invalid time range in query string", () => {
        location.search = "time_range=testTimeRange&start_time=1&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    it("getTimeRangeDurationFromQueryString should return null for invalid start time in query string", () => {
        location.search = "time_range=CUSTOM&start_time=-1&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    it("getTimeRangeDurationFromQueryString should return null for invalid end time in query string", () => {
        location.search = "time_range=CUSTOM&start_time=1&end_time=testEndTime";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    it("getTimeRangeDurationFromQueryString should return appropriate time range duration from query string", () => {
        location.search = "timeRange=CUSTOM&startTime=1&endTime=2";

        expect(getTimeRangeDurationFromQueryString()).toEqual(
            mockTimeRangeDuration
        );
    });

    it("setQueryString should not set query string for invalid key", () => {
        const setQueryParamsUtils = useSetQueryParamsUtil();
        setQueryParamsUtils.setQueryString(
            null as unknown as string,
            "testValue"
        );

        expect(mockSetQueryParamFunc).not.toHaveBeenCalled();
    });

    it("setQueryString should not set query string for empty key", () => {
        const setQueryParamsUtils = useSetQueryParamsUtil();
        setQueryParamsUtils.setQueryString("", "testValue");

        expect(mockSetQueryParamFunc).not.toHaveBeenCalled();
    });

    it("setQueryString should set appropriate query string for key", () => {
        const setQueryParamsUtils = useSetQueryParamsUtil();
        setQueryParamsUtils.setQueryString("testKey", "testValue");

        expect(mockSetQueryParamFunc).toHaveBeenCalledWith(
            new URLSearchParams("testKey=testValue"),
            { replace: true }
        );
    });

    it("setQueryString should set appropriate query string for existing key", () => {
        urlParams = new URLSearchParams(
            "testKey1=testValue1&testKey2=testValue2"
        );
        const setQueryParamsUtils = useSetQueryParamsUtil();
        setQueryParamsUtils.setQueryString("testKey1", "testValue3");

        expect(mockSetQueryParamFunc).toHaveBeenCalledWith(
            new URLSearchParams("testKey1=testValue3&testKey2=testValue2"),
            { replace: true }
        );
    });

    it("getQueryString should return empty string when key is not found in query string", () => {
        location.search = "";

        expect(getQueryString("testKey")).toEqual("");
    });

    it("getQueryString should return appropriate value from query string for key", () => {
        location.search = "testKey=testValue";

        expect(getQueryString("testKey")).toEqual("testValue");
    });

    it("getRecognizedQueryString should return appropriate query string", () => {
        location.search =
            "timeRange=CUSTOM&startTime=1&endTime=2&search=testSearchValue&testKey=testValue";

        expect(getRecognizedQuery().toString()).toEqual(
            "timeRange=CUSTOM&startTime=1&endTime=2"
        );
    });

    it("isValidNumberId should return false for invalid string", () => {
        expect(isValidNumberId(null as unknown as string)).toBeFalsy();
    });

    it("isValidNumberId should return false for empty string", () => {
        expect(isValidNumberId("")).toBeFalsy();
    });

    it("isValidNumberId should return false for non-numeric string", () => {
        expect(isValidNumberId("testString")).toBeFalsy();
    });

    it("isValidNumberId should return true for positive integer string", () => {
        expect(isValidNumberId("1")).toBeTruthy();
    });

    it("isValidNumberId should return true for 0 string", () => {
        expect(isValidNumberId("0")).toBeTruthy();
    });

    it("isValidNumberId should return false for negative integer string", () => {
        expect(isValidNumberId("-1")).toBeFalsy();
    });

    it("isValidNumberId should return false for decimal number string", () => {
        expect(isValidNumberId("1.1")).toBeFalsy();
    });

    it("serializeKeyValuePair should return sorted serialized string", () => {
        expect(
            serializeKeyValuePair([
                {
                    key: "z",
                    value: "3",
                },
                {
                    key: "a",
                    value: "1",
                },
                {
                    key: "z",
                    value: "2",
                },
                {
                    key: "c",
                    value: "2=4",
                },
                {
                    key: "z",
                    value: "1",
                },
            ])
        ).toEqual("a='1',c='2=4',z='1',z='2',z='3'");
    });

    it("serializeKeyValuePair should return empty string when empty array is passed", () => {
        expect(serializeKeyValuePair([])).toEqual("");
    });

    it("deserializeKeyValuePair should return correct values for `=` in values", () => {
        expect(deserializeKeyValuePair("a=1,c=2=4,z=3")).toEqual([
            {
                key: "a",
                value: "1",
            },
            {
                key: "c",
                value: "2=4",
            },
            {
                key: "z",
                value: "3",
            },
        ]);
    });

    it("deserializeKeyValuePair should return empty array for empty string", () => {
        expect(deserializeKeyValuePair("")).toEqual([]);
    });
});

const mockTimeRangeDuration = {
    timeRange: TimeRange.CUSTOM,
    startTime: 1,
    endTime: 2,
};

const mockSetQueryParamFunc = jest.fn();
let urlParams: URLSearchParams;
