/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { createTheme, makeStyles } from "@material-ui/core";
import { lightV1 } from "../../../platform/utils";
import { ColorV1 } from "../../../platform/utils/material-ui/color.util";
import {
    paletteOptionsV1,
    PaletteV1,
} from "../../../platform/utils/material-ui/palette.util";

export const easyAlertStyles = makeStyles((theme) => ({
    sqlButton: {
        padding: 0,
        margin: "10px 0",
        color: "#0097A9",
    },
    backgroundContainer: {
        backgroundColor: ColorV1.White1,
        padding: "30px",
    },
    card: {
        borderRadius: "8px",
        border: "1px solid #B4CDE0",
        padding: "24px",
        width: "100%",
        alignItems: "center",
    },
    container: {
        padding: "30px",
    },
    header: {
        fontWeight: 700,
        fontSize: "27px",
    },
    inputHeader: {
        fontWeight: 700,
    },
    infoButton: {
        height: "36px",
        backgroundColor: "#f3f9ff",
        textWrap: "nowrap",
        textTransform: "none",
    },
    algorithmContainer: {
        marginBottom: theme.spacing(2),
    },
    button: {
        borderRadius: "8px",
        height: "32px",
        textTransform: "none",
    },
    footer: {
        border: `1px solid ${ColorV1.Grey10}`,
        borderRadius: "0 0 8px 8px",
        overflow: "hidden",
        backgroundColor: ColorV1.Grey9,
        padding: "8px 12px",
        display: "flex",

        "& > button": {
            backgroundColor: ColorV1.White1,
            border: `1px solid ${ColorV1.Grey10}`,
            borderRadius: 8,
        },
    },
    textArea: {
        width: "100%",
        borderRadius: "8px 8px 0 0",
        paddingTop: "5px",
        paddingLeft: theme.spacing(1),
    },
    textAreaContainer: {
        gap: 0,
        display: "flex",
        flexDirection: "column",
    },
    recommendedAlgorithmContainer: {
        display: "flex",
        alignItems: "center",
        gap: 1,
    },
    checkCircleIcon: {
        color: ColorV1.Green2,
        marginRight: theme.spacing(1),
        fontSize: "12px",
    },
    recommendedAlgorithmText: {
        fontWeight: 700,
    },
    cancelIcon: {
        color: ColorV1.Red2,
        marginRight: theme.spacing(1),
        fontSize: "12px",
    },
    detectionRecommendationsContainer: {
        marginLeft: theme.spacing(1),
    },
    detectionRecommendationsReadyText: {
        color: ColorV1.Green2,
    },
    detectionRecommendationsFailedText: {
        color: ColorV1.Red2,
    },
    animatedBorder: {
        "& .MuiOutlinedInput-root": {
            "& fieldset": {
                animation: "$pulse 2s infinite",
            },
        },
    },
    "@keyframes pulse": {
        "0%": {
            borderColor: theme.palette.primary.main,
            borderWidth: "1px",
        },
        "50%": {
            borderColor: theme.palette.error.main,
            borderWidth: "1px",
        },
        "100%": {
            borderColor: theme.palette.primary.main,
            borderWidth: "1px",
        },
    },
}));

export const createAlertPageTheme = createTheme({
    ...lightV1,
    overrides: {
        ...lightV1.overrides,
        MuiCard: {
            ...lightV1.overrides?.MuiCard,
            root: {
                ...lightV1.overrides?.MuiCard?.root,
                borderRadius: "8px",
                border: `1px solid ${PaletteV1.BorderColorGrey}`,
            },
        },
        MuiButton: {
            ...lightV1.overrides?.MuiButton,
            root: {
                ...lightV1.overrides?.MuiButton?.root,
                borderRadius: "8px",
                textTransform: "none",
            },
        },
    },
    palette: {
        ...paletteOptionsV1,
        primary: {
            light: ColorV1.Blue1,
            main: "#003D86",
            dark: ColorV1.Blue3,
            contrastText: ColorV1.White1,
        },
    },
    typography: {
        ...lightV1.typography,
        h1: {
            ...lightV1.typography.h1,
            fontWeight: 700,
        },
        h2: {
            ...lightV1.typography.h2,
            fontWeight: 700,
        },
        h3: {
            ...lightV1.typography.h3,
            fontWeight: 700,
        },
        h4: {
            ...lightV1.typography.h4,
            fontWeight: 700,
        },
        h5: {
            ...lightV1.typography.h5,
            fontWeight: 700,
        },
        h6: {
            ...lightV1.typography.h6,
            fontWeight: 700,
        },
    },
});
