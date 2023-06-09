/*
 * Copyright 2023 StarTree Inc
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
import { sortBy } from "lodash";
import { AnomalyFilterOption } from "../../components/rca/anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import {
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisMetricRow,
} from "../../rest/dto/rca.interfaces";

export const RCA_NO_FILTER_MARKER = "(NO_FILTER)";

export const getFilterDimensionAnalysisData = (
    data: AnomalyDimensionAnalysisData
): AnomalyDimensionAnalysisData => {
    return {
        ...data,
        responseRows: data.responseRows.map(
            (responseRow: AnomalyDimensionAnalysisMetricRow) => ({
                ...responseRow,
                names: responseRow.names.filter(
                    (name) => name !== RCA_NO_FILTER_MARKER
                ),
            })
        ),
    };
};

const FILTER_OPTION_SORT_KEYS = ["key", "value"];

export const areFiltersEqual = (
    filterOption: AnomalyFilterOption[],
    otherFilterOption: AnomalyFilterOption[]
): boolean => {
    const sortedFilterOption = sortBy(filterOption, FILTER_OPTION_SORT_KEYS);
    const sortedOtherFilterOption = sortBy(
        otherFilterOption,
        FILTER_OPTION_SORT_KEYS
    );

    for (let i = 0; i < sortedFilterOption.length; i++) {
        if (
            sortedFilterOption[i].key !== sortedOtherFilterOption[i].key ||
            sortedFilterOption[i].value !== sortedOtherFilterOption[i].value
        ) {
            return false;
        }
    }

    return true;
};
