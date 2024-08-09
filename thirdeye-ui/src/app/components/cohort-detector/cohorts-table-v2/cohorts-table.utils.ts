/*
 * Copyright 2024 StarTree Inc
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

import { CohortResult } from "../../../rest/dto/rca.interfaces";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
import { generateFilterOptions } from "../../rca/top-contributors-table/top-contributors-table.utils";
import { CohortTableRowData } from "./cohorts-table.interfaces";

export const NAME_JOIN_KEY = " AND ";
export const PERCENTAGE = "percentage";

// Stringifies the dimension filter object for `queryData=` use
export const getCohortTableRowFromData = <
    T extends Pick<CohortResult, "dimensionFilters"> = CohortTableRowData
>(
    cohortItem: T
): T & { name: string } => {
    const copied: T & { name: string } = { ...cohortItem, name: "" };

    const values: string[] = [];
    const columnKeys: string[] = [];
    Object.keys(cohortItem.dimensionFilters).forEach((dimensionColumn) => {
        values.push(cohortItem.dimensionFilters[dimensionColumn]);
        columnKeys.push(dimensionColumn);
    });

    copied.name = generateFilterOptions(values, columnKeys, [])
        .map((item) => concatKeyValueWithEqual(item, true))
        .sort()
        .join(NAME_JOIN_KEY);

    return copied;
};
