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
import { sortBy } from "lodash";
import { Series } from "../time-series-chart.interfaces";

export const sortSeries = (series: Series[]): Series[] => {
    const noIndices: Series[] = [];
    let withIndices: Series[] = [];

    series.forEach((seriesData: Series) => {
        if (seriesData.legendIndex === undefined) {
            noIndices.push(seriesData);
        } else {
            withIndices.push(seriesData);
        }
    });

    withIndices = sortBy(withIndices, (item) => item.legendIndex);

    return [...noIndices, ...withIndices];
};
