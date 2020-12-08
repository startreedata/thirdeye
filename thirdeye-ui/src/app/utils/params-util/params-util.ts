import { isInteger, toNumber } from "lodash";
import { appHistory } from "../history-util/history-util";

export enum AppQueryStringKey {
    SEARCH = "SEARCH",
}

export const setSearchQueryString = (value: string): void => {
    setQueryString(AppQueryStringKey.SEARCH.toLowerCase(), value);
};

export const getSearchQueryString = (): string => {
    return getQueryString(AppQueryStringKey.SEARCH.toLowerCase());
};

export const setQueryString = (key: string, value: string): void => {
    if (!key) {
        return;
    }

    // Get existing query strings
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
