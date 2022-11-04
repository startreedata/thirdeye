import {
    OFFSET_REGEX_EXTRACT,
    OFFSET_TO_HUMAN_READABLE,
} from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { OFFSET_TO_MILLISECONDS } from "../time/time.util";

export const comparisonOffsetReadableValue = (offsetString: string): string => {
    const result = OFFSET_REGEX_EXTRACT.exec(offsetString);

    if (result === null) {
        return "could not parse offset";
    }

    const [, valueStr, unit] = result;

    if (Number(valueStr) === 1) {
        return `${valueStr} ${OFFSET_TO_HUMAN_READABLE[
            unit
        ].toLowerCase()} ago`;
    }

    return `${valueStr} ${OFFSET_TO_HUMAN_READABLE[unit].toLowerCase()}s ago`;
};

export const baselineOffsetToMilliseconds = (offsetString: string): number => {
    const result = OFFSET_REGEX_EXTRACT.exec(offsetString);

    if (result === null) {
        return 0;
    }

    const [, valueStr, unit] = result;

    return Number(valueStr) * OFFSET_TO_MILLISECONDS[unit];
};
