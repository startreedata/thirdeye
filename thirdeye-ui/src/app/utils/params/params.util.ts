import { isInteger, toNumber } from "lodash";
import { URLSearchParamsInit } from "react-router-dom";
import { SearchQueryStringKey } from "../../components/search-bar/search-bar.interfaces";
import {
    TimeRange,
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { createTimeRangeDuration } from "../time-range/time-range.util";

type SetQueryStringFunc = (
    nextInit: URLSearchParamsInit,
    navigateOptions?: {
        replace?: boolean | undefined;
    }
) => void;

export const HASH_KEY_ACCESS_TOKEN = "access_token";

export const getAccessTokenFromHashParams = (): string => {
    let accessToken = "";
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    if (urlSearchParams.has(HASH_KEY_ACCESS_TOKEN)) {
        accessToken = urlSearchParams.get(HASH_KEY_ACCESS_TOKEN) as string;
    }

    return accessToken;
};

export const setSearchInQueryString = (
    search: string,
    setQueryStringFunc: SetQueryStringFunc
): void => {
    setQueryString(
        SearchQueryStringKey.SEARCH.toLowerCase(),
        search,
        setQueryStringFunc
    );
};

export const getSearchFromQueryString = (): string => {
    return getQueryString(SearchQueryStringKey.SEARCH.toLowerCase());
};

export const setSearchTextInQueryString = (
    searchText: string,
    setQueryStringFunc: SetQueryStringFunc
): void => {
    setQueryString(
        SearchQueryStringKey.SEARCH_TEXT.toLowerCase(),
        searchText,
        setQueryStringFunc
    );
};

export const getSearchTextFromQueryString = (): string => {
    return getQueryString(SearchQueryStringKey.SEARCH_TEXT.toLowerCase());
};

export const setTimeRangeDurationInQueryString = (
    timeRangeDuration: TimeRangeDuration,
    setQueryStringFunc: SetQueryStringFunc
): void => {
    if (!timeRangeDuration) {
        return;
    }

    setQueryString(
        TimeRangeQueryStringKey.TIME_RANGE.toLowerCase(),
        timeRangeDuration.timeRange,
        setQueryStringFunc
    );
    setQueryString(
        TimeRangeQueryStringKey.START_TIME.toLowerCase(),
        timeRangeDuration.startTime.toString(),
        setQueryStringFunc
    );
    setQueryString(
        TimeRangeQueryStringKey.END_TIME.toLowerCase(),
        timeRangeDuration.endTime.toString(),
        setQueryStringFunc
    );
};

export const getTimeRangeDurationFromQueryString =
    (): TimeRangeDuration | null => {
        const timeRangeString = getQueryString(
            TimeRangeQueryStringKey.TIME_RANGE.toLowerCase()
        );

        const startTimeString = getQueryString(
            TimeRangeQueryStringKey.START_TIME.toLowerCase()
        );
        const startTime = toNumber(startTimeString);

        const endTimeString = getQueryString(
            TimeRangeQueryStringKey.END_TIME.toLowerCase()
        );
        const endTime = toNumber(endTimeString);

        // Validate time range duration
        if (
            !TimeRange[timeRangeString as keyof typeof TimeRange] ||
            !startTimeString ||
            !isInteger(startTime) ||
            startTime < 0 ||
            !endTimeString ||
            !isInteger(endTime) ||
            endTime < 0
        ) {
            return null;
        }

        return createTimeRangeDuration(
            TimeRange[timeRangeString as keyof typeof TimeRange],
            startTime,
            endTime
        );
    };

export const setQueryString = (
    key: string,
    value: string,
    setQueryStringFunc: SetQueryStringFunc
): void => {
    if (!key) {
        return;
    }

    // Get existing query string
    const urlSearchParams = new URLSearchParams(location.search);
    urlSearchParams.set(key, value);

    // Update URL
    setQueryStringFunc(urlSearchParams, { replace: true });
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
    const currentURLSearchParams = new URLSearchParams(location.search);
    const recognizedURLSearchParams = new URLSearchParams();
    for (const allowedAppQueryStringKey of allowedAppQueryStringKeys) {
        if (
            currentURLSearchParams.has(allowedAppQueryStringKey.toLowerCase())
        ) {
            recognizedURLSearchParams.set(
                allowedAppQueryStringKey.toLowerCase(),
                currentURLSearchParams.get(
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
    TimeRangeQueryStringKey.TIME_RANGE,
    TimeRangeQueryStringKey.START_TIME,
    TimeRangeQueryStringKey.END_TIME,
];
