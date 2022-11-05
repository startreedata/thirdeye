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
    formatLargeNumberV1,
    formatNumberV1,
    formatPercentageV1,
    stylePercentageV1,
} from "./number.util";

jest.mock("numbro", () =>
    jest.fn().mockImplementation((num) => ({
        format: mockFormat.mockReturnValue(num.toString()),
    }))
);

describe("Number Util", () => {
    it("formatNumberV1 should return empty string for invalid number", () => {
        expect(formatNumberV1(null as unknown as number)).toEqual("");
        expect(mockFormat).not.toHaveBeenCalled();
    });

    it("formatNumberV1 should invoke numbro.format with default input and return appropriate string for number", () => {
        expect(formatNumberV1(1)).toEqual("1");
        expect(mockFormat).toHaveBeenCalledWith({
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    it("formatNumberV1 should invoke numbro.format with appropriate input and return appropriate string for number", () => {
        expect(formatNumberV1(0, 1, true)).toEqual("0");
        expect(mockFormat).toHaveBeenNthCalledWith(1, {
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: true,
        });
        expect(formatNumberV1(0, 1, false)).toEqual("0");
        expect(mockFormat).toHaveBeenNthCalledWith(2, {
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: false,
        });
    });

    it("formatLargeNumberV1 should return empty string for invalid number", () => {
        expect(formatLargeNumberV1(null as unknown as number)).toEqual("");
        expect(mockFormat).not.toHaveBeenCalled();
    });

    it("formatLargeNumberV1 should invoke numbro.format with default input and return appropriate string for number", () => {
        expect(formatLargeNumberV1(1)).toEqual("1");
        expect(mockFormat).toHaveBeenCalledWith({
            average: true,
            lowPrecision: false,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    it("formatLargeNumberV1 should invoke numbro.format with appropriate input and return appropriate string for number", () => {
        expect(formatLargeNumberV1(0, 1, false)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            average: true,
            lowPrecision: false,
            mantissa: 1,
            optionalMantissa: false,
        });
        expect(formatLargeNumberV1(0, 1, true)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            average: true,
            lowPrecision: false,
            mantissa: 1,
            optionalMantissa: true,
        });
    });

    it("formatPercentageV1 should return empty string for invalid number", () => {
        expect(formatPercentageV1(null as unknown as number)).toEqual("");
        expect(mockFormat).not.toHaveBeenCalled();
    });

    it("formatPercentageV1 should invoke numbro.format with default input and return appropriate string for number", () => {
        expect(formatPercentageV1(1)).toEqual("1");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    it("formatPercentageV1 should invoke numbro.format with appropriate input and return appropriate string for number", () => {
        expect(formatPercentageV1(0, 1, false)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: false,
        });
        expect(formatPercentageV1(0, 1, true)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: true,
        });
    });

    it("stylePercentageV1 should return empty string for invalid number", () => {
        expect(stylePercentageV1(null as unknown as number)).toEqual("");
        expect(mockFormat).not.toHaveBeenCalled();
    });

    it("stylePercentageV1 should invoke numbro.format with default input and return appropriate string for number", () => {
        expect(stylePercentageV1(1)).toEqual("0.01");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    it("stylePercentageV1 should invoke numbro.format with appropriate input and return appropriate string for number", () => {
        expect(stylePercentageV1(0, 1, false)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: false,
        });
        expect(stylePercentageV1(0, 1, true)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: true,
        });
    });
});

const mockFormat = jest.fn();
