import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    getAccessTokenFromHashParams,
    getQueryString,
    getRecognizedQuery,
    getSearchFromQueryString,
    getSearchTextFromQueryString,
    getTimeRangeDurationFromQueryString,
    isValidNumberId,
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
            new URLSearchParams("search_text=testSearchTextValue"),
            { replace: true }
        );
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
            new URLSearchParams("time_range=CUSTOM&start_time=1&end_time=2"),
            { replace: true }
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
            "time_range=CUSTOM&start_time=1&end_time=2&search=testSearchValue&testKey=testValue";

        expect(getRecognizedQuery().toString()).toEqual(
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

const mockSetQueryParamFunc = jest.fn();
let urlParams: URLSearchParams;
