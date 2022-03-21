import { isFinite, isNil } from "lodash";
import numbro from "numbro";

const MANTISSA = 2;
const OPTIONAL_MANTISSA = true;

// Returns formatted string representation of number
// For example:
// 10 -> 10
// 1000 -> 1,000
// 10.0 -> 10
// 10.1 -> 10.10
// 10.123 -> 10.12
// 10.127 -> 10.13
export const formatNumber = (
    num: number,
    mantissa?: number,
    optionalMantissa?: boolean
): string => {
    if (isNil(num) || !isFinite(num)) {
        return "";
    }

    return numbro(num).format({
        thousandSeparated: true,
        mantissa: !isNil(mantissa) ? mantissa : MANTISSA,
        optionalMantissa: !isNil(optionalMantissa)
            ? optionalMantissa
            : OPTIONAL_MANTISSA,
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
export const formatLargeNumber = (
    num: number,
    mantissa?: number,
    optionalMantissa?: boolean
): string => {
    if (isNil(num) || !isFinite(num)) {
        return "";
    }

    return numbro(num).format({
        average: true,
        lowPrecision: false,
        mantissa: !isNil(mantissa) ? mantissa : MANTISSA,
        optionalMantissa: !isNil(optionalMantissa)
            ? optionalMantissa
            : OPTIONAL_MANTISSA,
    } as numbro.Format);
};

// Returns percentage string representation of number
// For example:
// 1 -> 100%
// 10 -> 1,000%
// 0.1 -> 10%
// 0.01234 -> 1.23%
// 0.01237 -> 1.24%
export const formatPercentage = (
    num: number,
    mantissa?: number,
    optionalMantissa?: boolean
): string => {
    if (isNil(num) || !isFinite(num)) {
        return "";
    }

    return numbro(num).format({
        output: "percent",
        thousandSeparated: true,
        mantissa: !isNil(mantissa) ? mantissa : MANTISSA,
        optionalMantissa: !isNil(optionalMantissa)
            ? optionalMantissa
            : OPTIONAL_MANTISSA,
    });
};
