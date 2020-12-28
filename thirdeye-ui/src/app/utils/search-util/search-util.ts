import flatten from "flat";
import { isEmpty } from "lodash";

// Traverses all the properties of object, including those nested until it finds a string property
// for which match function returns true
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
