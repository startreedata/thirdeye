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
