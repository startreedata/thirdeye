/*
 * Copyright 2023 StarTree Inc
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
import { PaletteV1 } from "../../platform/utils";
import { ColorV1 } from "../../platform/utils/material-ui/color.util";

export const useAdditonalFiltersDrawerStyles = makeStyles((theme) => ({
    drawerPaper: {
        backgroundColor: theme.palette.background.paper,
        maxWidth: 280,
        width: "100%",

        "& form": {
            height: "100%",
            display: "flex",
            flexDirection: "column",
        },
    },
    header: {
        padding: "10px 12px",
        marginTop: theme.spacing(6),

        "& h6": {
            color: PaletteV1.NavBarBackgroundColor,
        },
    },
    content: {
        borderBottom: `1px solid ${ColorV1.Grey10}`,
        borderTop: `1px solid ${ColorV1.Grey10}`,
        padding: "20px 12px",
        flex: "1 1 auto",
        overflowY: "auto",
        height: 0,
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: 500,
        color: PaletteV1.NavBarBackgroundColor,
    },
    footer: {
        padding: "10px 12px",
        backgroundColor: ColorV1.Grey9,
    },
    actionSecondary: {
        textTransform: "none",
        backgroundColor: ColorV1.White1,
        border: `1px solid ${ColorV1.Grey10}`,
    },
    actionPrimary: {
        textTransform: "none",
    },
    configItem: {
        "& h6": {
            color: PaletteV1.NavBarBackgroundColor,
            fontWeight: 700,
            fontSize: 16,
            marginBottom: 6,
        },
    },
    configItemFields: {
        display: "flex",
        flexDirection: "column",
        gap: 4,
    },
    formLabel: {
        display: "flex",
        justifyContent: "flex-start",
        alignItems: "center",
        gap: 8,
        fontWeight: 700,
        fontSize: 12,
        color: PaletteV1.NavBarBackgroundColor,
        marginBottom: 4,
    },
}));
