// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import {
    booleanSortComparatorV1,
    numberSortComparatorV1,
    stringSortComparatorV1,
} from "./comparator.util";

describe("Comparator Util", () => {
    it("stringSortComparatorV1 should sort string array appropriately for default order", () => {
        const stringArray = [
            "aa",
            "z",
            "A",
            null as unknown as string,
            "c",
            "",
            ".",
            "AA",
        ];

        expect(
            stringArray.sort((data1, data2) =>
                stringSortComparatorV1(data1, data2)
            )
        ).toEqual(["", ".", "A", "aa", "AA", "c", "z", null]);
    });

    it("stringSortComparatorV1 should sort string array appropriately for ascending order", () => {
        const stringArray = [
            "aa",
            "z",
            "A",
            null as unknown as string,
            "c",
            "",
            ".",
            "AA",
        ];

        expect(
            stringArray.sort((data1, data2) =>
                stringSortComparatorV1(data1, data2, "asc")
            )
        ).toEqual(["", ".", "A", "aa", "AA", "c", "z", null]);
    });

    it("stringSortComparatorV1 should sort string array appropriately for descending order", () => {
        const stringArray = [
            "aa",
            "z",
            "A",
            null as unknown as string,
            "c",
            "",
            ".",
            "AA",
        ];

        expect(
            stringArray.sort((data1, data2) =>
                stringSortComparatorV1(data1, data2, "desc")
            )
        ).toEqual(["z", "c", "aa", "AA", "A", null, ".", ""]);
    });

    it("numberSortComparatorV1 should sort number array appropriately for default order", () => {
        const numberArray = [10, 0, -0.5, 0.5, 0, 10, -5];

        expect(
            numberArray.sort((data1, data2) =>
                numberSortComparatorV1(data1, data2)
            )
        ).toEqual([-5, -0.5, 0, 0, 0.5, 10, 10]);
    });

    it("numberSortComparatorV1 should sort number array appropriately for ascending order", () => {
        const numberArray = [10, 0, -0.5, 0.5, 0, 10, -5];

        expect(
            numberArray.sort((data1, data2) =>
                numberSortComparatorV1(data1, data2, "asc")
            )
        ).toEqual([-5, -0.5, 0, 0, 0.5, 10, 10]);
    });

    it("numberSortComparatorV1 should sort number array appropriately for descending order", () => {
        const numberArray = [10, 0, -0.5, 0.5, 0, 10, -5];

        expect(
            numberArray.sort((data1, data2) =>
                numberSortComparatorV1(data1, data2, "desc")
            )
        ).toEqual([10, 10, 0.5, 0, 0, -0.5, -5]);
    });

    it("booleanSortComparatorV1 should sort boolean array appropriately for default order", () => {
        const booleanArray = [true, false, false, true, true];

        expect(
            booleanArray.sort((data1, data2) =>
                booleanSortComparatorV1(data1, data2)
            )
        ).toEqual([true, true, true, false, false]);
    });

    it("booleanSortComparatorV1 should sort boolean array appropriately for ascending order", () => {
        const booleanArray = [true, false, false, true, true];

        expect(
            booleanArray.sort((data1, data2) =>
                booleanSortComparatorV1(data1, data2, "asc")
            )
        ).toEqual([true, true, true, false, false]);
    });

    it("booleanSortComparatorV1 should sort boolean array appropriately for descending order", () => {
        const booleanArray = [true, false, false, true, true];

        expect(
            booleanArray.sort((data1, data2) =>
                booleanSortComparatorV1(data1, data2, "desc")
            )
        ).toEqual([false, false, true, true, true]);
    });
});
