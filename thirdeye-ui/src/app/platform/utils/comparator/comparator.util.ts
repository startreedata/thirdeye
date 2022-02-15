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
