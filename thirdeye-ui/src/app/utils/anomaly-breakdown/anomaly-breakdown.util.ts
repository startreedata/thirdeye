/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
