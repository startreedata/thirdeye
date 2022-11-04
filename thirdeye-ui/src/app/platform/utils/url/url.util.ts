import { endsWith, replace, startsWith } from "lodash";

export const addLeadingForwardSlashV1 = (url: string): string => {
    if (!url) {
        return "/";
    }

    if (startsWith(url, "/")) {
        return url;
    }

    return `/${url}`;
};

export const removeLeadingForwardSlashV1 = (url: string): string => {
    return replace(url, new RegExp("^/"), "");
};

export const addTrailingForwardSlashV1 = (url: string): string => {
    if (!url) {
        return "/";
    }

    if (endsWith(url, "/")) {
        return url;
    }

    return `${url}/`;
};

export const removeTrailingForwardSlashV1 = (url: string): string => {
    return replace(url, new RegExp("/$"), "");
};
