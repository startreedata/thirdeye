/*
 * Copyright 2022 StarTree Inc
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

import { isNumber } from "lodash";
import { EditableAnomaly } from "../../../pages/anomalies-create-page/anomalies-create-page.interfaces";
import {
    Alert,
    EnumerationItemConfig,
} from "../../../rest/dto/alert.interfaces";

export const getEnumerationItemsConfigFromAlert = (
    alert: Alert
): EnumerationItemConfig[] | null => {
    if (
        alert?.templateProperties?.enumerationItems &&
        (alert?.templateProperties?.enumerationItems as EnumerationItemConfig[])
            .length > 0
    ) {
        return alert?.templateProperties
            ?.enumerationItems as EnumerationItemConfig[];
    }

    return null;
};

export const AlertId = "alertId";

export const getIsAnomalyValid = (
    editableAnomaly: EditableAnomaly
): boolean => {
    const { alert, startTime, endTime } = editableAnomaly;

    // Basic sanity checks for values
    const conditions = [
        isNumber(alert?.id),
        isNumber(startTime),
        isNumber(endTime),
        startTime > 0,
        endTime > 0,
        endTime > startTime,
    ];

    // The anomaly is valid iff all check are valid
    return conditions.every((c) => !!c);
};
