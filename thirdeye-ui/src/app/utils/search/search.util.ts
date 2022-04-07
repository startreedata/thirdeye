import flatten from "flat";
import i18n from "i18next";
import { isEmpty, isNil } from "lodash";
import { formatNumberV1 } from "../../platform/utils";

// Traverses all the properties of object, including those nested, in arrays and maps until it finds
// a string property for which match function returns true
export const deepSearchStringProperty = <T>(
    object: T,
    matchFn: (value: string) => boolean
): string | null => {
    if (isEmpty(object) || typeof object !== "object") {
        return null;
    }

    const flattenedObject = flatten(object);
    for (const value of Object.values(
        flattenedObject as Record<string, unknown>
    )) {
        if (value && typeof value === "string" && matchFn && matchFn(value)) {
            return value;
        }
    }

    return null;
};

export const getSearchStatusLabel = (count: number, total: number): string => {
    return i18n.t("label.search-count", {
        count: formatNumberV1(!isNil(count) ? count : 0) as never,
        total: formatNumberV1(!isNil(total) ? total : 0) as never,
    });
};

export const getSelectedStatusLabel = (count: number): string => {
    return i18n.t("label.selected-count", {
        count: formatNumberV1(!isNil(count) ? count : 0) as never,
    });
};
