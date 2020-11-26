import numbro from "numbro";
import {
    formatLargeNumber,
    formatNumber,
    formatPercentage,
} from "./number-util";

jest.mock("numbro");

const mockNumbro = {
    format: jest.fn().mockReturnValue("testNumberFormat"),
};

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

    test("formatNumber shall invoke numbro.format with appropriate input and return result", () => {
        const numberString = formatNumber(1);

        expect(numbro).toHaveBeenCalledWith(1);
        expect(mockNumbro.format).toHaveBeenCalledWith({
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
        expect(numberString).toEqual("testNumberFormat");
    });

    test("formatLargeNumber shall invoke numbro.format with appropriate input and return result", () => {
        const numberString = formatLargeNumber(1);

        expect(numbro).toHaveBeenCalledWith(1);
        expect(mockNumbro.format).toHaveBeenCalledWith({
            average: true,
            lowPrecision: false,
        });
        expect(numberString).toEqual("testNumberFormat");
    });

    test("formatPercentage shall invoke numbro.format with appropriate input and return result", () => {
        const numberString = formatPercentage(1);

        expect(numbro).toHaveBeenCalledWith(1);
        expect(mockNumbro.format).toHaveBeenCalledWith({
            output: "percent",
            thousandSeparated: true,
            mantissa: 2,
            optionalMantissa: true,
        });
        expect(numberString).toEqual("testNumberFormat");
    });
});
