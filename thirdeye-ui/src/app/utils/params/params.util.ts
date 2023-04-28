/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { isInteger, isNumber, toNumber } from "lodash";
import { AnomalyFilterOption } from "../../components/rca/anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";
// import { SearchQueryStringKey } from "../../components/search-bar/search-bar.interfaces";
import {
    TimeRange,
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { createTimeRangeDuration } from "../time-range/time-range.util";

const SINGLE_SEARCH_PARAM_SEPARATOR = ",";

export const QUERY_PARAM_KEY_FOR_EXPANDED = "expanded";
export const HASH_KEY_ACCESS_TOKEN = "access_token";

export const getAccessTokenFromHashParams = (): string => {
    let accessToken = "";
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    if (urlSearchParams.has(HASH_KEY_ACCESS_TOKEN)) {
        accessToken = urlSearchParams.get(HASH_KEY_ACCESS_TOKEN) as string;
    }

    return accessToken;
};

export const getTimeRangeDurationFromQueryString =
    (): TimeRangeDuration | null => {
        const timeRangeString = getQueryString(
            TimeRangeQueryStringKey.TIME_RANGE
        );

        const startTimeString = getQueryString(
            TimeRangeQueryStringKey.START_TIME
        );
        const startTime = toNumber(startTimeString);

        const endTimeString = getQueryString(TimeRangeQueryStringKey.END_TIME);
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
export const getRecognizedQuery = (): URLSearchParams => {
    const currentURLSearchParams = new URLSearchParams(location.search);
    const recognizedURLSearchParams = new URLSearchParams();
    for (const allowedAppQueryStringKey of allowedAppQueryStringKeys) {
        if (currentURLSearchParams.has(allowedAppQueryStringKey)) {
            recognizedURLSearchParams.set(
                allowedAppQueryStringKey,
                currentURLSearchParams.get(allowedAppQueryStringKey) as string
            );
        }
    }

    return recognizedURLSearchParams;
};

export const isValidNumberId = (param: string): boolean => {
    if (!param) {
        return false;
    }

    const numberId = toNumber(param);

    return isInteger(numberId) && numberId >= 0;
};

export const concatKeyValueWithEqual = (
    filterOption: AnomalyFilterOption,
    shouldQuoteStrings = false
): string => {
    if (isNumber(filterOption.value) || !shouldQuoteStrings) {
        return `${filterOption.key}=${filterOption.value}`;
    }

    return `${filterOption.key}='${filterOption.value}'`;
};

/**
 * Serialize and array of objects such as:
 * [{
 *     key: "city",
 *     value: "sunnyvale"
 * },{
 *     key: "country",
 *     value: "usa"
 * }]
 *
 * to `city=sunnyvalye,country=usa`
 *
 * @param {array} keysValues - array of objects with "key" and "value" properties
 */
export const serializeKeyValuePair = (
    keysValues: AnomalyFilterOption[],
    shouldQuoteStrings = false
): string => {
    return keysValues
        .map((item) => concatKeyValueWithEqual(item, shouldQuoteStrings))
        .sort()
        .join(SINGLE_SEARCH_PARAM_SEPARATOR);
};

/**
 * Deserialize a string like `city=sunnyvale,country=usa` to
 *
 * [{
 *     key: "city",
 *     value: "sunnyvale"
 * },{
 *     key: "country",
 *     value: "usa"
 * }]
 *
 * @param {string} serializedString - key values pairs seperated by commas
 */
export const deserializeKeyValuePair = (
    serializedString: string
): AnomalyFilterOption[] => {
    if (!serializedString) {
        return [];
    }

    const splitted = serializedString.split(SINGLE_SEARCH_PARAM_SEPARATOR);
    const parsed: AnomalyFilterOption[] = [];

    splitted.forEach((item) => {
        // account for key=value=value1
        const [key, ...values] = item.split("=");
        parsed.push({
            key,
            value: values.join("="),
        });
    });

    return parsed;
};

// List of app query string keys that are allowed to be carried forward when navigating
const allowedAppQueryStringKeys = [
    TimeRangeQueryStringKey.TIME_RANGE,
    TimeRangeQueryStringKey.START_TIME,
    TimeRangeQueryStringKey.END_TIME,
];
