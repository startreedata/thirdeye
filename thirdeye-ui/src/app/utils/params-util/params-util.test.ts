import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import {
    getQueryString,
    getRecognizedQueryString,
    getSearchFromQueryString,
    getSearchTextFromQueryString,
    getTimeRangeDurationFromQueryString,
    isValidNumberId,
    setQueryString,
    setSearchInQueryString,
    setSearchTextInQueryString,
    setTimeRangeDurationInQueryString,
} from "./params-util";

const systemLocation = location;

jest.mock("../history-util/history-util", () => ({
    appHistory: {
        replace: jest.fn().mockImplementation((locationObject) => {
            location.search = locationObject.search;
        }),
    },
}));

describe("Params Util", () => {
    beforeAll(() => {
        Object.defineProperty(window, "location", {
            value: {
                search: "",
            },
        });
    });

    afterAll(() => {
        Object.defineProperty(window, "location", {
            value: systemLocation,
        });
    });

    test("setSearchInQueryString should set appropriate search in query string", () => {
        location.search = "";
        setSearchInQueryString("testSearchValue");

        expect(location.search).toEqual("search=testSearchValue");
    });

    test("getSearchFromQueryString should return empty string when search is not found in query string", () => {
        location.search = "";

        expect(getSearchFromQueryString()).toEqual("");
    });

    test("getSearchFromQueryString should return appropriate search from query string", () => {
        location.search = "search=testSearchValue";

        expect(getSearchFromQueryString()).toEqual("testSearchValue");
    });

    test("setSearchTextInQueryString should set appropriate search text in query string", () => {
        location.search = "";
        setSearchTextInQueryString("testSearchTextValue");

        expect(location.search).toEqual("search_text=testSearchTextValue");
    });

    test("getSearchTextFromQueryString should return empty string when search text is not found in query string", () => {
        location.search = "";

        expect(getSearchTextFromQueryString()).toEqual("");
    });

    test("getSearchTextFromQueryString should return appropriate search text from query string", () => {
        location.search = "search_text=testSearchTextValue";

        expect(getSearchTextFromQueryString()).toEqual("testSearchTextValue");
    });

    test("setTimeRangeDurationInQueryString should not set invalid time range duration in query string", () => {
        location.search = "";
        setTimeRangeDurationInQueryString(
            (null as unknown) as TimeRangeDuration
        );

        expect(location.search).toEqual("");
    });

    test("setTimeRangeDurationInQueryString should set appropriate time range duration in query string", () => {
        location.search = "";
        setTimeRangeDurationInQueryString(mockTimeRangeDuration);

        expect(location.search).toEqual(
            "time_range=CUSTOM&start_time=1&end_time=2"
        );
    });

    test("getTimeRangeDurationFromQueryString should return null when time range is not found in query string", () => {
        location.search = "";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    test("getTimeRangeDurationFromQueryString should return null when time range key is not found in query string", () => {
        location.search = "start_time=1&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    test("getTimeRangeDurationFromQueryString should return null when start time key is not found in query string", () => {
        location.search = "time_range=CUSTOM&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    test("getTimeRangeDurationFromQueryString should return null when end time key is not found in query string", () => {
        location.search = "time_range=CUSTOM&start_time=1";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    test("getTimeRangeDurationFromQueryString should return null for invalid time range in query string", () => {
        location.search =
            "time_range=testInvalidTimeRangeValue&start_time=1&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    test("getTimeRangeDurationFromQueryString should return null for invalid start time in query string", () => {
        location.search = "time_range=CUSTOM&start_time=-1&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    test("getTimeRangeDurationFromQueryString should return null for invalid end time in query string", () => {
        location.search =
            "time_range=CUSTOM&start_time=1&end_time=testInvalidEndTimeValue";

        expect(getTimeRangeDurationFromQueryString()).toBeNull();
    });

    test("getTimeRangeDurationFromQueryString should return appropriate time range from query string", () => {
        location.search = "time_range=CUSTOM&start_time=1&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toEqual(
            mockTimeRangeDuration
        );
    });

    test("setQueryString should not set query string for invalid key", () => {
        location.search = "";
        setQueryString((null as unknown) as string, "testValue");

        expect(location.search).toEqual("");
    });

    test("setQueryString should not set query string for empty key", () => {
        location.search = "";
        setQueryString("", "testValue");

        expect(location.search).toEqual("");
    });

    test("setQueryString should set appropriate query string for key", () => {
        location.search = "";
        setQueryString("testKey", "testValue");

        expect(location.search).toEqual("testKey=testValue");
    });

    test("setQueryString should set appropriate query string for existing key", () => {
        location.search = "testKey1=testValue1&testKey2=testValue2";
        setQueryString("testKey1", "testValue3");

        expect(location.search).toEqual(
            "testKey1=testValue3&testKey2=testValue2"
        );
    });

    test("getQueryString should return empty string for key not found in query string", () => {
        location.search = "";

        expect(getQueryString("testKey")).toEqual("");
    });

    test("getQueryString should return appropriate string for key", () => {
        location.search = "testKey=testValue";

        expect(getQueryString("testKey")).toEqual("testValue");
    });

    test("getRecognizedQueryString should return appropriate query string", () => {
        location.search =
            "time_range=CUSTOM&start_time=1&end_time=2&search=testSearchValue&testKey=testValue";

        expect(getRecognizedQueryString()).toEqual(
            "time_range=CUSTOM&start_time=1&end_time=2"
        );
    });

    test("isValidNumberId should return false for invalid string", () => {
        expect(isValidNumberId((null as unknown) as string)).toBeFalsy();
    });

    test("isValidNumberId should return false for empty string", () => {
        expect(isValidNumberId("")).toBeFalsy();
    });

    test("isValidNumberId should return false for non-numeric string", () => {
        expect(isValidNumberId("testString")).toBeFalsy();
    });

    test("isValidNumberId should return true for positive integer string", () => {
        expect(isValidNumberId("1")).toBeTruthy();
    });

    test("isValidNumberId should return true for 0 string", () => {
        expect(isValidNumberId("0")).toBeTruthy();
    });

    test("isValidNumberId should return false for negative integer string", () => {
        expect(isValidNumberId("-1")).toBeFalsy();
    });

    test("isValidNumberId should return false for decimal number string", () => {
        expect(isValidNumberId("1.1")).toBeFalsy();
    });
});

const mockTimeRangeDuration: TimeRangeDuration = {
    timeRange: TimeRange.CUSTOM,
    startTime: 1,
    endTime: 2,
};
