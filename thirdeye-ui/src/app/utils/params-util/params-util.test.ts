import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import { appHistory } from "../history-util/history-util";
import {
    getQueryString,
    getRecognizedQueryString,
    getSearchFromQueryString,
    getSearchTextFromQueryString,
    getTimeRangeFromQueryString,
    isValidNumberId,
    setQueryString,
    setSearchInQueryString,
    setSearchTextInQueryString,
    setTimeRangeInQueryString,
} from "./params-util";

jest.mock("../history-util/history-util", () => ({
    appHistory: {
        replace: jest.fn(),
    },
}));

describe("Params Util", () => {
    beforeEach(() => {
        // jsdom doesn't support navigation, workaround is to mock URLSearchParams
        jest.spyOn(URLSearchParams.prototype, "set").mockImplementation();
        jest.spyOn(URLSearchParams.prototype, "get").mockImplementation(
            (key: string): string => {
                return key;
            }
        );
        jest.spyOn(URLSearchParams.prototype, "delete").mockImplementation();
        jest.spyOn(URLSearchParams.prototype, "toString").mockReturnValue(
            "testUrlSearchParams"
        );
    });

    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("setSearchInQueryString should set appropriate search in query string", () => {
        setSearchInQueryString("testSearch");

        expect(URLSearchParams.prototype.set).toHaveBeenCalledWith(
            "search",
            "testSearch"
        );
        expect(appHistory.replace).toHaveBeenNthCalledWith(1, {
            search: "testUrlSearchParams",
        });
    });

    test("getSearchFromQueryString should return empty string when search is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(false);

        expect(getSearchFromQueryString()).toEqual("");
        expect(URLSearchParams.prototype.has).toHaveBeenCalledWith("search");
        expect(URLSearchParams.prototype.get).not.toHaveBeenCalled();
    });

    test("getSearchFromQueryString should return appropriate search from query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);

        expect(getSearchFromQueryString()).toEqual("search");
    });

    test("setSearchTextInQueryString should set appropriate search text in query string", () => {
        setSearchTextInQueryString("testSearchText");

        expect(URLSearchParams.prototype.set).toHaveBeenCalledWith(
            "search_text",
            "testSearchText"
        );
        expect(appHistory.replace).toHaveBeenCalledWith({
            search: "testUrlSearchParams",
        });
    });

    test("getSearchTextFromQueryString should return empty string when search text is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(false);

        expect(getSearchTextFromQueryString()).toEqual("");
        expect(URLSearchParams.prototype.has).toHaveBeenCalledWith(
            "search_text"
        );
        expect(URLSearchParams.prototype.get).not.toHaveBeenCalled();
    });

    test("getSearchTextFromQueryString should return appropriate search text from query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);

        expect(getSearchTextFromQueryString()).toEqual("search_text");
    });

    test("setTimeRangeInQueryString should not set invalid time range in query string", () => {
        setTimeRangeInQueryString((null as unknown) as TimeRangeDuration);

        expect(URLSearchParams.prototype.set).not.toHaveBeenCalled();
        expect(appHistory.replace).not.toHaveBeenCalled();
    });

    test("setTimeRangeInQueryString should set appropriate time range in query string", () => {
        setTimeRangeInQueryString({
            timeRange: TimeRange.CUSTOM,
            startTime: 1,
            endTime: 2,
        });

        expect(URLSearchParams.prototype.set).toHaveBeenCalledTimes(3);
        expect(URLSearchParams.prototype.set).toHaveBeenNthCalledWith(
            1,
            "time_range",
            "CUSTOM"
        );
        expect(URLSearchParams.prototype.set).toHaveBeenNthCalledWith(
            2,
            "start_time",
            "1"
        );
        expect(URLSearchParams.prototype.set).toHaveBeenNthCalledWith(
            3,
            "end_time",
            "2"
        );
        expect(appHistory.replace).toHaveBeenCalledTimes(3);
        expect(appHistory.replace).toHaveBeenNthCalledWith(1, {
            search: "testUrlSearchParams",
        });
        expect(appHistory.replace).toHaveBeenNthCalledWith(2, {
            search: "testUrlSearchParams",
        });
        expect(appHistory.replace).toHaveBeenNthCalledWith(3, {
            search: "testUrlSearchParams",
        });
    });

    test("getTimeRangeFromQueryString should return null when time range is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(false);

        expect(getTimeRangeFromQueryString()).toBeNull();
        expect(URLSearchParams.prototype.has).toHaveBeenCalledTimes(3);
        expect(URLSearchParams.prototype.has).toHaveBeenNthCalledWith(
            1,
            "time_range"
        );
        expect(URLSearchParams.prototype.has).toHaveBeenNthCalledWith(
            2,
            "start_time"
        );
        expect(URLSearchParams.prototype.has).toHaveBeenNthCalledWith(
            3,
            "end_time"
        );
    });

    test("getTimeRangeFromQueryString should return null when time range key is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has")
            .mockReturnValueOnce(false)
            .mockReturnValueOnce(true)
            .mockReturnValueOnce(true);

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString should return null when start time key is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has")
            .mockReturnValueOnce(true)
            .mockReturnValueOnce(false)
            .mockReturnValueOnce(true);

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString should return null when end time key is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has")
            .mockReturnValueOnce(true)
            .mockReturnValueOnce(true)
            .mockReturnValueOnce(false);

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString should return null for invalid time range in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);
        jest.spyOn(URLSearchParams.prototype, "get")
            .mockReturnValueOnce("invalidTimeRange")
            .mockReturnValueOnce("1")
            .mockReturnValueOnce("2");

        expect(getTimeRangeFromQueryString()).toBeNull();
        expect(URLSearchParams.prototype.get).toHaveBeenCalledTimes(3);
        expect(URLSearchParams.prototype.get).toHaveBeenNthCalledWith(
            1,
            "time_range"
        );
        expect(URLSearchParams.prototype.get).toHaveBeenNthCalledWith(
            2,
            "start_time"
        );
        expect(URLSearchParams.prototype.get).toHaveBeenNthCalledWith(
            3,
            "end_time"
        );
    });

    test("getTimeRangeFromQueryString should return null for invalid start time in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);
        jest.spyOn(URLSearchParams.prototype, "get")
            .mockReturnValueOnce("TODAY")
            .mockReturnValueOnce("-1")
            .mockReturnValueOnce("2");

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString should return null for invalid end time in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);
        jest.spyOn(URLSearchParams.prototype, "get")
            .mockReturnValueOnce("TODAY")
            .mockReturnValueOnce("1")
            .mockReturnValueOnce("testInvalidEndTime");

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString should return appropriate time range from query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);
        jest.spyOn(URLSearchParams.prototype, "get")
            .mockReturnValueOnce("TODAY")
            .mockReturnValueOnce("1")
            .mockReturnValueOnce("2");

        expect(getTimeRangeFromQueryString()).toEqual({
            timeRange: "TODAY",
            startTime: 1,
            endTime: 2,
        });
    });

    test("setQueryString should not set query string for invalid key", () => {
        setQueryString((null as unknown) as string, "testValue");

        expect(URLSearchParams.prototype.set).not.toHaveBeenCalled();
        expect(appHistory.replace).not.toHaveBeenCalled();
    });

    test("setQueryString should not set query string for empty key", () => {
        setQueryString("", "testValue");

        expect(URLSearchParams.prototype.set).not.toHaveBeenCalled();
        expect(appHistory.replace).not.toHaveBeenCalled();
    });

    test("setQueryString should set appropriate query string for key", () => {
        setQueryString("testKey", "testValue");

        expect(URLSearchParams.prototype.set).toHaveBeenCalledWith(
            "testKey",
            "testValue"
        );
        expect(appHistory.replace).toHaveBeenCalledWith({
            search: "testUrlSearchParams",
        });
    });

    test("getQueryString should return empty string for key not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(false);

        expect(getQueryString("testKey")).toEqual("");
        expect(URLSearchParams.prototype.has).toHaveBeenCalledWith("testKey");
        expect(URLSearchParams.prototype.get).not.toHaveBeenCalled();
    });

    test("getQueryString should return appropriate string for key", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);

        expect(getQueryString("testKey")).toEqual("testKey");
    });

    test("getRecognizedQueryString should return query string with only the recognized and allowed key value pairs", () => {
        jest.spyOn(URLSearchParams.prototype, "forEach").mockImplementation(
            (
                callbackFn: (
                    value: string,
                    key: string,
                    parent: URLSearchParams
                ) => void
            ): void => {
                // Iterate with a recognized, an unrecognized and a query string that is not allowed
                callbackFn(
                    "testTimeRangeValue",
                    "time_range",
                    {} as URLSearchParams
                );
                callbackFn(
                    "testUnrecognizedValue",
                    "testUnrecognizedKey",
                    {} as URLSearchParams
                );
                callbackFn("testSearchValue", "search", {} as URLSearchParams);
            }
        );

        expect(getRecognizedQueryString()).toEqual("testUrlSearchParams");
        expect(URLSearchParams.prototype.delete).toHaveBeenCalledTimes(2);
        expect(URLSearchParams.prototype.delete).toHaveBeenNthCalledWith(
            1,
            "testUnrecognizedKey"
        );
        expect(URLSearchParams.prototype.delete).toHaveBeenNthCalledWith(
            2,
            "search"
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
