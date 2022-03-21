import { TypographyOptions } from "@material-ui/core/styles/createTypography";

// Material-UI theme typography
export const typographyOptions: TypographyOptions = {
    fontFamily: "Source Sans Pro, sans-serif",
    fontWeightLight: 300,
    fontWeightRegular: 400,
    fontWeightMedium: 600,
    fontWeightBold: 700,
    h1: {
        fontSize: 48,
        fontWeight: 600,
    },
    h2: {
        fontSize: 40,
        fontWeight: 600,
    },
    h3: {
        fontSize: 36,
        fontWeight: 600,
    },
    h4: {
        fontSize: 32,
        fontWeight: 600,
    },
    h5: {
        fontSize: 24,
        fontWeight: 600,
    },
    h6: {
        fontSize: 20,
        fontWeight: 600,
    },
    subtitle1: {
        fontSize: 16,
        fontWeight: 600,
    },
    subtitle2: {
        fontSize: 14,
        fontWeight: 600,
    },
    body1: {
        fontSize: 16,
        fontWeight: 400,
    },
    body2: {
        fontSize: 14,
        fontWeight: 400,
    },
    button: {
        fontSize: 14,
        fontWeight: 400,
    },
    caption: {
        fontSize: 12,
        fontWeight: 400,
    },
    overline: {
        fontSize: 12,
        fontWeight: 400,
        lineHeight: 1,
        letterSpacing: 1,
    },
};

export const codeTypographyOptions = {
    body2: {
        fontFamily: "Source Code Pro, monospace",
        ...typographyOptions.body2,
    },
};
