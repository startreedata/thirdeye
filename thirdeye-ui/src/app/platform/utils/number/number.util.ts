import { isNil } from "lodash";
import numbro from "numbro";

const MANTISSA_DEFAULT = 2;
const OPTIONAL_MANTISSA_DEFAULT = true;

// Returns formatted string representation of number
// For example:
// 10 -> 10
// 1000 -> 1,000
// 10.0 -> 10
// 10.1 -> 10.10
// 10.123 -> 10.12
// 10.127 -> 10.13
export const formatNumberV1 = (
    num: number,
    mantissa: number = MANTISSA_DEFAULT,
    optionalMantissa: boolean = OPTIONAL_MANTISSA_DEFAULT
): string => {
    if (isNil(num)) {
        return "";
    }

    return numbro(num).format({
        thousandSeparated: true,
        mantissa: mantissa,
        optionalMantissa: optionalMantissa,
    });
};

// Returns abbreviated string representation of number
// For example:
// 1 -> 1
// 10 -> 10
// 100 -> 100
// 1000 -> 1k
// 10000 -> 10k
// 100000 -> 100k
// 1000000 -> 1m
// 10000000 -> 10m
// 100000000 -> 100m
// 1000000000 -> 1b
// 10000000000 -> 10b
// 100000000000 -> 100b
// 1000000000000 -> 1t
// 10000000000000 -> 10t
// 100000000000000 -> 100t
export const formatLargeNumberV1 = (
    num: number,
    mantissa: number = MANTISSA_DEFAULT,
    optionalMantissa: boolean = OPTIONAL_MANTISSA_DEFAULT
): string => {
    if (isNil(num)) {
        return "";
    }

    return numbro(num).format({
        average: true,
        lowPrecision: false,
        mantissa: mantissa,
        optionalMantissa: optionalMantissa,
    });
};

// Returns percentage string representation of number (decimal to percentage conversion)
// For example:
// 1 -> 100%
// 10 -> 1,000%
// 0.1 -> 10%
// 0.01234 -> 1.23%
// 0.01237 -> 1.24%
export const formatPercentageV1 = (
    num: number,
    mantissa: number = MANTISSA_DEFAULT,
    optionalMantissa: boolean = OPTIONAL_MANTISSA_DEFAULT
): string => {
    if (isNil(num)) {
        return "";
    }

    return numbro(num).format({
        output: "percent",
        thousandSeparated: true,
        mantissa: mantissa,
        optionalMantissa: optionalMantissa,
    });
};

// Returns percentage string representation of number (number stylized as percentage)
// For example:
// 10 -> 10%
// 1000 -> 1,000%
// 10.0 -> 10%
// 10.1 -> 10.10%
// 10.123 -> 10.12%
// 10.127 -> 10.13%
export const stylePercentageV1 = (
    num: number,
    mantissa: number = MANTISSA_DEFAULT,
    optionalMantissa: boolean = OPTIONAL_MANTISSA_DEFAULT
): string => {
    if (isNil(num)) {
        return "";
    }

    return numbro(num / 100).format({
        output: "percent",
        thousandSeparated: true,
        mantissa: mantissa,
        optionalMantissa: optionalMantissa,
    });
};
