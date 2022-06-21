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
// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export const stringSortComparatorV1 = (
    data1: string,
    data2: string,
    order: "asc" | "desc" = "asc"
): number => {
    if (
        (data1 && data1.toLocaleLowerCase()) >
        (data2 && data2.toLocaleLowerCase())
    ) {
        return order === "asc" ? 1 : -1;
    }

    if (
        (data1 && data1.toLocaleLowerCase()) <
        (data2 && data2.toLocaleLowerCase())
    ) {
        return order === "asc" ? -1 : 1;
    }

    return 0;
};

export const numberSortComparatorV1 = (
    data1: number,
    data2: number,
    order: "asc" | "desc" = "asc"
): number => {
    if (data1 > data2) {
        return order === "asc" ? 1 : -1;
    }

    if (data1 < data2) {
        return order === "asc" ? -1 : 1;
    }

    return 0;
};

export const booleanSortComparatorV1 = (
    data1: boolean,
    data2: boolean,
    order: "asc" | "desc" = "asc"
): number => {
    if (data1 > data2) {
        return order === "asc" ? -1 : 1;
    }

    if (data1 < data2) {
        return order === "asc" ? 1 : -1;
    }

    return 0;
};
