import {
    formatLargeNumber,
    formatNumber,
    formatPercentage,
} from "./number.util";

jest.mock("numbro", () =>
    jest.fn().mockImplementation((num) => ({
        format: mockFormat.mockReturnValue(num.toString()),
    }))
);

describe("Number Util", () => {
    it("formatNumber should return empty string for invald number", () => {
        expect(formatNumber((null as unknown) as number)).toEqual("");
        expect(mockFormat).not.toHaveBeenCalled();
    });

    it("formatNumber should invoke numbro.format with default input and return appropriate string for number", () => {
        expect(formatNumber(1)).toEqual("1");
        expect(mockFormat).toHaveBeenCalledWith({
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    it("formatNumber should invoke numbro.format with appropriate input and return appropriate string for number", () => {
        expect(formatNumber(0, 1, true)).toEqual("0");
        expect(mockFormat).toHaveBeenNthCalledWith(1, {
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: true,
        });
        expect(formatNumber(0, 1, false)).toEqual("0");
        expect(mockFormat).toHaveBeenNthCalledWith(2, {
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: false,
        });
    });

    it("formatLargeNumber should return empty string for invald number", () => {
        expect(formatLargeNumber((null as unknown) as number)).toEqual("");
        expect(mockFormat).not.toHaveBeenCalled();
    });

    it("formatLargeNumber should invoke numbro.format with default input and return appropriate string for number", () => {
        expect(formatLargeNumber(1)).toEqual("1");
        expect(mockFormat).toHaveBeenCalledWith({
            average: true,
            lowPrecision: false,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    it("formatLargeNumber should invoke numbro.format with appropriate input and return appropriate string for number", () => {
        expect(formatLargeNumber(0, 1, false)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            average: true,
            lowPrecision: false,
            mantissa: 1,
            optionalMantissa: false,
        });
        expect(formatLargeNumber(0, 1, true)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            average: true,
            lowPrecision: false,
            mantissa: 1,
            optionalMantissa: true,
        });
    });

    it("formatPercentage should return empty string for invalid number", () => {
        expect(formatPercentage((null as unknown) as number)).toEqual("");
        expect(mockFormat).not.toHaveBeenCalled();
    });

    it("formatPercentage should invoke numbro.format with default input and return appropriate string for number", () => {
        expect(formatPercentage(1)).toEqual("1");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    it("formatPercentage should invoke numbro.format with appropriate input and return appropriate string for number", () => {
        expect(formatPercentage(0, 1, false)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: false,
        });
        expect(formatPercentage(0, 1, true)).toEqual("0");
        expect(mockFormat).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: true,
        });
    });
});

const mockFormat = jest.fn();
