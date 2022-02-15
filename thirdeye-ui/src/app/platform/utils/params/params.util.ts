// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { historyV1 } from "../history/history.util";

export const getQueryParamV1 = (key: string): string => {
    let value = "";
    const urlSearchParams = new URLSearchParams(location.search);
    if (urlSearchParams.has(key)) {
        value = urlSearchParams.get(key) as string;
    }

    return value;
};

export const setQueryParamV1 = (key: string, value: string): void => {
    if (!key) {
        return;
    }

    // Get existing query string
    const urlSearchParams = new URLSearchParams(location.search);
    urlSearchParams.set(key, value);

    // Update URL
    historyV1.replace({
        ...historyV1.location,
        search: urlSearchParams.toString(),
    });
};

export const clearQueryParamV1 = (key: string): void => {
    if (!key) {
        return;
    }

    // Get existing query string
    const urlSearchParams = new URLSearchParams(location.search);
    urlSearchParams.delete(key);

    // Update URL
    historyV1.replace({
        ...historyV1.location,
        search: urlSearchParams.toString(),
    });
};

export const getHashParamV1 = (key: string): string => {
    let value = "";
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    if (urlSearchParams.has(key)) {
        value = urlSearchParams.get(key) as string;
    }

    return value;
};

export const setHashParamV1 = (key: string, value: string): void => {
    if (!key) {
        return;
    }

    // Get existing hash string
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    urlSearchParams.set(key, value);

    // Update URL
    historyV1.replace({
        ...historyV1.location,
        hash: `#${urlSearchParams.toString()}`,
    });
};

export const clearHashParamV1 = (key: string): void => {
    if (!key) {
        return;
    }

    // Get existing hash string
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    urlSearchParams.delete(key);

    // Update URL
    historyV1.replace({
        ...historyV1.location,
        hash: urlSearchParams.toString()
            ? `#${urlSearchParams.toString()}`
            : "",
    });
};
