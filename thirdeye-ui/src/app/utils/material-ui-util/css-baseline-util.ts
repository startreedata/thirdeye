import {
    sourceSansProBold,
    sourceSansProLight,
    sourceSansProRegular,
    sourceSansProSemiBold,
} from "./font-util";

// CortexData Material-UI theme style overrides for CssBaseline
export const cssBaselineClasses = {
    "@global": {
        "@font-face": [
            sourceSansProLight,
            sourceSansProRegular,
            sourceSansProSemiBold,
            sourceSansProBold,
        ],
    },
};
