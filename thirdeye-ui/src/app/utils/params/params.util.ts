import { isInteger, toNumber } from "lodash";
import { useSearchParams } from "react-router-dom";
import { SearchQueryStringKey } from "../../components/search-bar/search-bar.interfaces";
import {
    TimeRange,
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { createTimeRangeDuration } from "../time-range/time-range.util";

export const HASH_KEY_ACCESS_TOKEN = "access_token";

interface UseSetQueryParamsUtil {
    setQueryString: (key: string, value: string) => void;
    setSearchInQueryString: (search: string) => void;
    setSearchTextInQueryString: (searchText: string) => void;
    setTimeRangeDurationInQueryString: (
        timeRangeDuration: TimeRangeDuration
    ) => void;
}

export function useSetQueryParamsUtil(): UseSetQueryParamsUtil {
    const [urlSearchParams, setSearchParams] = useSearchParams();

    const setQueryString = (key: string, value: string): void => {
        if (!key) {
            return;
        }

        // Get existing query string
        urlSearchParams.set(key, value);

        // Update URL
        setSearchParams(urlSearchParams, { replace: true });
    };

    return {
        setQueryString,
        setSearchInQueryString: (search: string): void => {
            setQueryString(SearchQueryStringKey.SEARCH.toLowerCase(), search);
        },
        setSearchTextInQueryString: (searchText: string): void => {
            setQueryString(
                SearchQueryStringKey.SEARCH_TEXT.toLowerCase(),
                searchText
            );
        },
        setTimeRangeDurationInQueryString: (
            timeRangeDuration: TimeRangeDuration
        ): void => {
            if (!timeRangeDuration) {
                return;
            }

            setQueryString(
                TimeRangeQueryStringKey.TIME_RANGE.toLowerCase(),
                timeRangeDuration.timeRange
            );
            setQueryString(
                TimeRangeQueryStringKey.START_TIME.toLowerCase(),
                timeRangeDuration.startTime.toString()
            );
            setQueryString(
                TimeRangeQueryStringKey.END_TIME.toLowerCase(),
                timeRangeDuration.endTime.toString()
            );
        },
    };
}

export const getAccessTokenFromHashParams = (): string => {
    let accessToken = "";
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    if (urlSearchParams.has(HASH_KEY_ACCESS_TOKEN)) {
        accessToken = urlSearchParams.get(HASH_KEY_ACCESS_TOKEN) as string;
    }

    return accessToken;
};

export const getSearchFromQueryString = (): string => {
    return getQueryString(SearchQueryStringKey.SEARCH.toLowerCase());
};

export const getSearchTextFromQueryString = (): string => {
    return getQueryString(SearchQueryStringKey.SEARCH_TEXT.toLowerCase());
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
