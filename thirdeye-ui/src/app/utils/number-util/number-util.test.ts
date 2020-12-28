import numbro from "numbro";
import {
    formatLargeNumber,
    formatNumber,
    formatPercentage,
} from "./number-util";

jest.mock("numbro");

describe("Number Util", () => {
    beforeAll(() => {
        ((numbro as unknown) as jest.Mock).mockReturnValue(mockNumbro);
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("formatNumber shall invoke numbro.format with appropriate default input and return result", () => {
        const numberString = formatNumber(1);

        expect(numberString).toEqual("testNumberFormat");
        expect(numbro).toHaveBeenCalledWith(1);
        expect(mockNumbro.format).toHaveBeenCalledWith({
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    test("formatNumber shall invoke numbro.format with appropriate input and return result", () => {
        const numberString = formatNumber(1, 1, false);

        expect(numberString).toEqual("testNumberFormat");
        expect(numbro).toHaveBeenCalledWith(1);
        expect(mockNumbro.format).toHaveBeenCalledWith({
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: false,
        });
    });

    test("formatLargeNumber shall invoke numbro.format with appropriate input and return result", () => {
        const numberString = formatLargeNumber(1);

        expect(numberString).toEqual("testNumberFormat");
        expect(numbro).toHaveBeenCalledWith(1);
        expect(mockNumbro.format).toHaveBeenCalledWith({
            average: true,
            lowPrecision: false,
        });
    });

    test("formatPercentage shall invoke numbro.format with appropriate default input and return result", () => {
        const numberString = formatPercentage(1);

        expect(numberString).toEqual("testNumberFormat");
        expect(numbro).toHaveBeenCalledWith(1);
        expect(mockNumbro.format).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
    });

    test("formatPercentage shall invoke numbro.format with appropriate input and return result", () => {
        const numberString = formatPercentage(1, 1, false);

        expect(numberString).toEqual("testNumberFormat");
        expect(numbro).toHaveBeenCalledWith(1);
        expect(mockNumbro.format).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 1,
            optionalMantissa: false,
        });
    });
});

const mockNumbro = {
    format: jest.fn().mockReturnValue("testNumberFormat"),
};
