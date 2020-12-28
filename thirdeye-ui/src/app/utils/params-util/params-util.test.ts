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

describe("Params Util", () => {
    beforeAll(() => {
        jest.spyOn(appHistory, "replace").mockImplementation();

        // jsdom doesn't support navigation, workaround is to mock URLSearchParams
        jest.spyOn(URLSearchParams.prototype, "set").mockImplementation();
        jest.spyOn(URLSearchParams.prototype, "get").mockReturnValue(
            "testUrlSearchParam"
        );
        jest.spyOn(URLSearchParams.prototype, "delete").mockImplementation();
        jest.spyOn(URLSearchParams.prototype, "toString").mockReturnValue(
            "testUrlSearchParams"
        );
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("setSearchInQueryString shall set appropriate search in query string", () => {
        setSearchInQueryString("testSearch");

        expect(URLSearchParams.prototype.set).toHaveBeenCalledWith(
            "search",
            "testSearch"
        );
        expect(appHistory.replace).toHaveBeenNthCalledWith(1, {
            search: "testUrlSearchParams",
        });
    });

    test("getSearchFromQueryString shall return empty string if search is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(false);

        expect(getSearchFromQueryString()).toEqual("");
        expect(URLSearchParams.prototype.has).toHaveBeenCalledWith("search");
    });

    test("getSearchFromQueryString shall return appropriate search from query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);

        expect(getSearchFromQueryString()).toEqual("testUrlSearchParam");
        expect(URLSearchParams.prototype.get).toHaveBeenCalledWith("search");
    });

    test("setSearchTextInQueryString shall set appropriate search text in query string", () => {
        setSearchTextInQueryString("testSearchText");

        expect(URLSearchParams.prototype.set).toHaveBeenCalledWith(
            "search_text",
            "testSearchText"
        );
        expect(appHistory.replace).toHaveBeenCalledWith({
            search: "testUrlSearchParams",
        });
    });

    test("getSearchTextFromQueryString shall return empty string if search text is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(false);

        expect(getSearchTextFromQueryString()).toEqual("");
        expect(URLSearchParams.prototype.has).toHaveBeenCalledWith(
            "search_text"
        );
    });

    test("getSearchTextFromQueryString shall return appropriate search text from query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);

        expect(getSearchTextFromQueryString()).toEqual("testUrlSearchParam");
        expect(URLSearchParams.prototype.get).toHaveBeenCalledWith(
            "search_text"
        );
    });

    test("setTimeRangeInQueryString shall not set invalid time range in query string", () => {
        setTimeRangeInQueryString((null as unknown) as TimeRangeDuration);

        expect(URLSearchParams.prototype.set).not.toHaveBeenCalled();
        expect(appHistory.replace).not.toHaveBeenCalled();
    });

    test("setTimeRangeInQueryString shall set appropriate time range in query string", () => {
        const mockTimeRangeDuration: TimeRangeDuration = {
            timeRange: TimeRange.CUSTOM,
            startTime: 1,
            endTime: 2,
        };
        setTimeRangeInQueryString(mockTimeRangeDuration);

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

    test("getTimeRangeFromQueryString shall return null if time range is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(false);

        expect(getTimeRangeFromQueryString()).toBeNull();
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

    test("getTimeRangeFromQueryString shall return null if time range key is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has")
            .mockReturnValueOnce(false)
            .mockReturnValueOnce(true)
            .mockReturnValueOnce(true);

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString shall return null if start time key is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has")
            .mockReturnValueOnce(true)
            .mockReturnValueOnce(false)
            .mockReturnValueOnce(true);

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString shall return null if end time key is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has")
            .mockReturnValueOnce(true)
            .mockReturnValueOnce(true)
            .mockReturnValueOnce(false);

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString shall return null if time range in query string is invalid", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);
        jest.spyOn(URLSearchParams.prototype, "get")
            .mockReturnValueOnce("invalidTimeRange")
            .mockReturnValueOnce("1")
            .mockReturnValueOnce("2");

        expect(getTimeRangeFromQueryString()).toBeNull();
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

    test("getTimeRangeFromQueryString shall return null if start time in query string is invalid", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);
        jest.spyOn(URLSearchParams.prototype, "get")
            .mockReturnValueOnce("TODAY")
            .mockReturnValueOnce("-1")
            .mockReturnValueOnce("2");

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString shall return null if end time in query string is invalid", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);
        jest.spyOn(URLSearchParams.prototype, "get")
            .mockReturnValueOnce("TODAY")
            .mockReturnValueOnce("1")
            .mockReturnValueOnce("testInvalidEndTime");

        expect(getTimeRangeFromQueryString()).toBeNull();
    });

    test("getTimeRangeFromQueryString shall return appropriate time range from query string", () => {
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

    test("setQueryString shall not set query string if key is invalid", () => {
        setQueryString((null as unknown) as string, "testValue");

        expect(URLSearchParams.prototype.set).not.toHaveBeenCalled();
        expect(appHistory.replace).not.toHaveBeenCalled();
    });

    test("setQueryString shall not set query string if key is empty", () => {
        setQueryString("", "testValue");

        expect(URLSearchParams.prototype.set).not.toHaveBeenCalled();
        expect(appHistory.replace).not.toHaveBeenCalled();
    });

    test("setQueryString shall set appropriate query string", () => {
        setQueryString("testKey", "testValue");

        expect(URLSearchParams.prototype.set).toHaveBeenCalledWith(
            "testKey",
            "testValue"
        );
        expect(appHistory.replace).toHaveBeenCalledWith({
            search: "testUrlSearchParams",
        });
    });

    test("getQueryString shall return empty string if key is not found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(false);

        expect(getQueryString("testKey")).toEqual("");
        expect(URLSearchParams.prototype.has).toHaveBeenCalledWith("testKey");
        expect(URLSearchParams.prototype.get).not.toHaveBeenCalled();
    });

    test("getQueryString shall return appropriate string if key is found in query string", () => {
        jest.spyOn(URLSearchParams.prototype, "has").mockReturnValue(true);

        expect(getQueryString("testKey")).toEqual("testUrlSearchParam");
        expect(URLSearchParams.prototype.has).toHaveBeenCalledWith("testKey");
        expect(URLSearchParams.prototype.get).toHaveBeenCalledWith("testKey");
    });

    test("getRecognizedQueryString shall return query string with recognized key value pairs", () => {
        jest.spyOn(URLSearchParams.prototype, "forEach").mockImplementation(
            (
                callbackFn: (
                    value: string,
                    key: string,
                    parent: URLSearchParams
                ) => void
            ): void => {
                // Iterate with a recognized and an unrecognized query string
                callbackFn("searchValue", "search", {} as URLSearchParams);
                callbackFn(
                    "testUnrecognizedValue",
                    "testUnrecognizedKey",
                    {} as URLSearchParams
                );
            }
        );

        expect(getRecognizedQueryString()).toEqual("testUrlSearchParams");
        expect(URLSearchParams.prototype.delete).toHaveBeenCalledWith(
            "testUnrecognizedKey"
        );
    });

    test("isValidNumberId shall return false if string is invalid", () => {
        expect(isValidNumberId((null as unknown) as string)).toBeFalsy();
    });

    test("isValidNumberId shall return false if string is empty", () => {
        expect(isValidNumberId("")).toBeFalsy();
    });

    test("isValidNumberId shall return false if string is non-numeric", () => {
        expect(isValidNumberId("testString")).toBeFalsy();
    });

    test("isValidNumberId shall return true if string is a positive integer", () => {
        expect(isValidNumberId("1")).toBeTruthy();
    });

    test("isValidNumberId shall return true if string is 0", () => {
        expect(isValidNumberId("0")).toBeTruthy();
    });

    test("isValidNumberId shall return false if string is a negative integer", () => {
        expect(isValidNumberId("-1")).toBeFalsy();
    });

    test("isValidNumberId shall return false if string is a decimal number", () => {
        expect(isValidNumberId("1.1")).toBeFalsy();
    });
});
