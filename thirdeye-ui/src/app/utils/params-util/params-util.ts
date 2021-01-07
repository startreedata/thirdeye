import { isInteger, toNumber } from "lodash";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import { appHistory } from "../history-util/history-util";
import { createTimeRangeDuration } from "../time-range-util/time-range-util";

export enum AppQueryStringKey {
    SEARCH = "SEARCH",
    SEARCH_TEXT = "SEARCH_TEXT",
    TIME_RANGE = "TIME_RANGE",
    START_TIME = "START_TIME",
    END_TIME = "END_TIME",
}

export const setSearchInQueryString = (search: string): void => {
    setQueryString(AppQueryStringKey.SEARCH.toLowerCase(), search);
};

export const getSearchFromQueryString = (): string => {
    return getQueryString(AppQueryStringKey.SEARCH.toLowerCase());
};

export const setSearchTextInQueryString = (searchText: string): void => {
    setQueryString(AppQueryStringKey.SEARCH_TEXT.toLowerCase(), searchText);
};

export const getSearchTextFromQueryString = (): string => {
    return getQueryString(AppQueryStringKey.SEARCH_TEXT.toLowerCase());
};

export const setTimeRangeInQueryString = (
    timeRangeDuration: TimeRangeDuration
): void => {
    if (!timeRangeDuration) {
        return;
    }

    setQueryString(
        AppQueryStringKey.TIME_RANGE.toLowerCase(),
        timeRangeDuration.timeRange
    );
    setQueryString(
        AppQueryStringKey.START_TIME.toLowerCase(),
        timeRangeDuration.startTime.toString()
    );
    setQueryString(
        AppQueryStringKey.END_TIME.toLowerCase(),
        timeRangeDuration.endTime.toString()
    );
};

export const getTimeRangeFromQueryString = (): TimeRangeDuration | null => {
    const timeRange = getQueryString(
        AppQueryStringKey.TIME_RANGE.toLowerCase()
    );
    const startTime = toNumber(
        getQueryString(AppQueryStringKey.START_TIME.toLowerCase())
    );
    const endTime = toNumber(
        getQueryString(AppQueryStringKey.END_TIME.toLowerCase())
    );

    // Validate time range
    if (
        !TimeRange[timeRange as keyof typeof TimeRange] ||
        !isFinite(startTime) ||
        startTime < 0 ||
        !isFinite(endTime) ||
        endTime < 0
    ) {
        return null;
    }

    return createTimeRangeDuration(
        TimeRange[timeRange as keyof typeof TimeRange],
        startTime,
        endTime
    );
};

export const setQueryString = (key: string, value: string): void => {
    if (!key) {
        return;
    }

    // Get existing query string
    const urlSearchParams = new URLSearchParams(location.search);
    urlSearchParams.set(key, value);

    // Update URL
    appHistory.replace({
        search: urlSearchParams.toString(),
    });
};

export const getQueryString = (key: string): string => {
    let value = "";

    const urlSearchParams = new URLSearchParams(location.search);
    if (urlSearchParams.has(key)) {
        value = urlSearchParams.get(key) as string;
    }

    return value;
};

// Returns current query string from URL with only the recognized and allowed key value pairs
export const getRecognizedQueryString = (): string => {
    const urlSearchParams = new URLSearchParams(location.search);
    urlSearchParams.forEach((_value: string, key: string): void => {
        const queryStringKey =
            AppQueryStringKey[
                key.toUpperCase() as keyof typeof AppQueryStringKey
            ];
        if (!queryStringKey) {
            // Unrecognized query string key
            urlSearchParams.delete(key);

            return;
        }

        if (
            queryStringKey === AppQueryStringKey.SEARCH ||
            queryStringKey === AppQueryStringKey.SEARCH_TEXT
        ) {
            // Search not allowed in query string
            urlSearchParams.delete(key);

            return;
        }
    });

    return urlSearchParams.toString();
};

export const isValidNumberId = (param: string): boolean => {
    if (!param) {
        return false;
    }

    const numberId = toNumber(param);

    return isInteger(numberId) && numberId >= 0;
};
