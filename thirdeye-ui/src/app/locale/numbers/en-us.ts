import numbro from "numbro";

export const enUS = {
    languageTag: "en-US",
    delimiters: {
        thousands: ",",
        decimal: ".",
    },
    abbreviations: {
        thousand: "k",
        million: "m",
        billion: "b",
        trillion: "t",
    },
    spaceSeparated: false,
    ordinal: (num: number): string => {
        const base = num % 10;

        return ~~((num % 100) / 10) === 1
            ? "th"
            : base === 1
            ? "st"
            : base === 2
            ? "nd"
            : base === 3
            ? "rd"
            : "th";
    },
    bytes: {
        binarySuffixes: [
            "B",
            "KiB",
            "MiB",
            "GiB",
            "TiB",
            "PiB",
            "EiB",
            "ZiB",
            "YiB",
        ],
        decimalSuffixes: ["B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"],
    },
    currency: {
        symbol: "$",
        position: "prefix",
        code: "USD",
    },
    currencyFormat: {
        thousandSeparated: true,
        totalLength: 4,
        spaceSeparated: true,
        spaceSeparatedCurrency: true,
    },
    formats: {
        fourDigits: {
            totalLength: 4,
            spaceSeparated: true,
        },
        fullWithTwoDecimals: {
            output: "currency",
            thousandSeparated: true,
            mantissa: 2,
        },
        fullWithTwoDecimalsNoCurrency: {
            thousandSeparated: true,
            mantissa: 2,
        },
        fullWithNoDecimals: {
            output: "currency",
            thousandSeparated: true,
            mantissa: 0,
        },
    },
} as numbro.NumbroLanguage;
