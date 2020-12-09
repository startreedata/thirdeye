import { isInteger, toNumber } from "lodash";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import { appHistory } from "../history-util/history-util";

export enum AppQueryStringKey {
    SEARCH = "SEARCH",
    TIME_RANGE = "TIME_RANGE",
    START_TIME = "START_TIME",
    END_TIME = "END_TIME",
}

export const setSearchInQueryString = (searchText: string): void => {
    setQueryString(AppQueryStringKey.SEARCH.toLowerCase(), searchText);
};

export const getSearchFromQueryString = (): string => {
    return getQueryString(AppQueryStringKey.SEARCH.toLowerCase());
};

export const setTimeRangeInQueryString = (
    timeRange: TimeRange,
    startTime: number,
    endTime: number
): void => {
    setQueryString(AppQueryStringKey.TIME_RANGE.toLowerCase(), timeRange);
    setQueryString(
        AppQueryStringKey.START_TIME.toLowerCase(),
        startTime.toString()
    );
    setQueryString(
        AppQueryStringKey.END_TIME.toLowerCase(),
        endTime.toString()
    );
};

export const getTimeRangeQueryString = (): string => {
    const timeRangeString = getQueryString(
        AppQueryStringKey.TIME_RANGE.toLowerCase()
    );
    const startTimeString = getQueryString(
        AppQueryStringKey.START_TIME.toLowerCase()
    );
    const endTimeString = getQueryString(
        AppQueryStringKey.END_TIME.toLowerCase()
    );

    // Validate time range query string
    if (!timeRangeString || !startTimeString || !endTimeString) {
        return "";
    }

    const urlSearchParams = new URLSearchParams();
    urlSearchParams.set(
        AppQueryStringKey.TIME_RANGE.toLowerCase(),
        timeRangeString
    );
    urlSearchParams.set(
        AppQueryStringKey.START_TIME.toLowerCase(),
        startTimeString
    );
    urlSearchParams.set(
        AppQueryStringKey.END_TIME.toLowerCase(),
        endTimeString
    );

    return urlSearchParams.toString();
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
        isNaN(startTime) ||
        startTime < 0 ||
        isNaN(endTime) ||
        endTime < 0
    ) {
        return null;
    }

    return {
        timeRange: TimeRange[timeRange as keyof typeof TimeRange],
        startTime: startTime,
        endTime: endTime,
    };
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

export const isValidNumberId = (param: string): boolean => {
    if (!param) {
        return false;
    }

    const numberId = toNumber(param);

    return isInteger(numberId) && numberId >= 0;
};
