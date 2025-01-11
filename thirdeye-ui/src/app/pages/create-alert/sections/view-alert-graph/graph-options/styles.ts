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
import { makeStyles } from "@material-ui/core";
import { ColorV1 } from "../../../../../platform/utils/material-ui/color.util";

export const graphOptionsStyles = makeStyles((theme) => ({
    card: {
        borderRadius: "8px",
        border: "1px solid #B4CDE0",
        padding: "24px",
        width: "100%",
        alignItems: "center",
        borderBottom: 0,
        borderBottomLeftRadius: 0,
        borderBottomRightRadius: 0,
    },
    algorithmContainer: {
        marginBottom: theme.spacing(2),
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
    recommendedAlgorithmContainer: {
        display: "flex",
        alignItems: "center",
        gap: 1,
    },
    detectionRecommendationsContainer: {
        marginLeft: theme.spacing(1),
    },
    recommendedAlgorithmText: {
        fontWeight: 700,
    },
    detectionRecommendationsReadyText: {
        color: ColorV1.Green2,
    },
    cancelIcon: {
        color: ColorV1.Red2,
        marginRight: theme.spacing(1),
        fontSize: "12px",
    },
    detectionRecommendationsFailedText: {
        color: ColorV1.Red2,
    },
    checkCircleIcon: {
        color: ColorV1.Green2,
        marginRight: theme.spacing(1),
        fontSize: "12px",
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
}));
