import { isInteger, toNumber } from "lodash";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
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

export const setTimeRangeDurationInQueryString = (
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

export const getTimeRangeDurationFromQueryString = (): TimeRangeDuration | null => {
    const timeRange = getQueryString(
        AppQueryStringKey.TIME_RANGE.toLowerCase()
    );
    const startTime = parseInt(
        getQueryString(AppQueryStringKey.START_TIME.toLowerCase())
    );
    const endTime = parseInt(
        getQueryString(AppQueryStringKey.END_TIME.toLowerCase())
    );

    // Validate time range duration
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

// Returns current query string from URL with only the recognized app query string key-value pairs
// that are allowed to be carried forward when navigating
export const getRecognizedQueryString = (): string => {
    const currentUrlSearchParams = new URLSearchParams(location.search);
    const recognizedURLSearchParams = new URLSearchParams();
    for (const allowedAppQueryStringKey of allowedAppQueryStringKeys) {
        if (
            currentUrlSearchParams.has(allowedAppQueryStringKey.toLowerCase())
        ) {
            recognizedURLSearchParams.set(
                allowedAppQueryStringKey.toLowerCase(),
                currentUrlSearchParams.get(
                    allowedAppQueryStringKey.toLowerCase()
                ) as string
            );
        }
    }

    return recognizedURLSearchParams.toString();
};

export const isValidNumberId = (param: string): boolean => {
    if (!param) {
        return false;
    }

    const numberId = toNumber(param);

    return isInteger(numberId) && numberId >= 0;
};

// List of app query string keys that are allowed to be carried forward when navigating
const allowedAppQueryStringKeys = [
    AppQueryStringKey.TIME_RANGE,
    AppQueryStringKey.START_TIME,
    AppQueryStringKey.END_TIME,
];
