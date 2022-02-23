import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    getAccessTokenFromHashParams,
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
} from "./params.util";

const systemLocation = location;

jest.mock("../../platform/utils", () => ({
    historyV1: {
        replace: jest.fn().mockImplementation((locationObject) => {
            location.hash = locationObject.hash;
            location.search = locationObject.search;
        }),
    },
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
        location.search = "";
        setSearchInQueryString("testSearchValue");

        expect(location.search).toEqual("search=testSearchValue");
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
        location.search = "";
        setSearchTextInQueryString("testSearchTextValue");

        expect(location.search).toEqual("search_text=testSearchTextValue");
    });

    it("getSearchTextFromQueryString should return empty string when search text is not found in query string", () => {
        location.search = "";

        expect(getSearchTextFromQueryString()).toEqual("");
    });

    it("getSearchTextFromQueryString should return appropriate search text from query string", () => {
        location.search = "search_text=testSearchTextValue";

        expect(getSearchTextFromQueryString()).toEqual("testSearchTextValue");
    });

    it("setTimeRangeDurationInQueryString should not set invalid time range duration in query string", () => {
        location.search = "";
        setTimeRangeDurationInQueryString(null as unknown as TimeRangeDuration);

        expect(location.search).toEqual("");
    });

    it("setTimeRangeDurationInQueryString should set appropriate time range duration in query string", () => {
        location.search = "";
        setTimeRangeDurationInQueryString(mockTimeRangeDuration);

        expect(location.search).toEqual(
            "time_range=CUSTOM&start_time=1&end_time=2"
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
        location.search = "time_range=CUSTOM&start_time=1&end_time=2";

        expect(getTimeRangeDurationFromQueryString()).toEqual(
            mockTimeRangeDuration
        );
    });

    it("setQueryString should not set query string for invalid key", () => {
        location.search = "";
        setQueryString(null as unknown as string, "testValue");

        expect(location.search).toEqual("");
    });

    it("setQueryString should not set query string for empty key", () => {
        location.search = "";
        setQueryString("", "testValue");

        expect(location.search).toEqual("");
    });

    it("setQueryString should set appropriate query string for key", () => {
        location.search = "";
        setQueryString("testKey", "testValue");

        expect(location.search).toEqual("testKey=testValue");
    });

    it("setQueryString should set appropriate query string for existing key", () => {
        location.search = "testKey1=testValue1&testKey2=testValue2";
        setQueryString("testKey1", "testValue3");

        expect(location.search).toEqual(
            "testKey1=testValue3&testKey2=testValue2"
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
            "time_range=CUSTOM&start_time=1&end_time=2&search=testSearchValue&testKey=testValue";

        expect(getRecognizedQueryString()).toEqual(
            "time_range=CUSTOM&start_time=1&end_time=2"
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
});

const mockTimeRangeDuration = {
    timeRange: TimeRange.CUSTOM,
    startTime: 1,
    endTime: 2,
};
