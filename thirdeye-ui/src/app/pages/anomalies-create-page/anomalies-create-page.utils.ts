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

import { AnomalyResultSource } from "../../rest/dto/anomaly.interfaces";
import { generateDateRangeDaysFromNow } from "../../utils/routes/routes.util";
import { EditableAnomaly } from "./anomalies-create-page.interfaces";

export const createEmptyAnomaly = (
    initialData?: Partial<EditableAnomaly>
): EditableAnomaly => {
    const [start, end] = generateDateRangeDaysFromNow(2);

    // TODO: Proper type
    const newAnomaly = {
        ...initialData,
        startTime: start,
        endTime: end,
        // alert:

        sourceType: AnomalyResultSource.USER_LABELED_ANOMALY,
    };

    return newAnomaly as EditableAnomaly;
};
