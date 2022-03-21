import {
    sourceCodeProRegular400,
    sourceSansProBold700,
    sourceSansProLight300,
    sourceSansProRegular400,
    sourceSansProSemiBold600,
} from "./font.util";

// Material-UI theme style overrides for CssBaseline
export const cssBaselineClasses = {
    "@global": {
        "@font-face": [
            sourceSansProLight300,
            sourceSansProRegular400,
            sourceSansProSemiBold600,
            sourceSansProBold700,
            sourceCodeProRegular400,
        ],
    },
};
