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
import { PaletteV1 } from "../../../platform/utils";

export const useRecommendedDocumentationV2Styles = makeStyles((theme) => ({
    gridContainer: {
        padding: 0,
    },
    imageCard: {
        backgroundColor: PaletteV1.BackgroundColorLight,
        padding: 0,
        borderRadius: 12,
        flexDirection: "column",
        display: "flex",
    },
    nonImageCard: {
        display: "flex",
        backgroundColor: PaletteV1.BackgroundColorLight,
        borderRadius: 12,
        border: `1px solid ${PaletteV1.BorderColorGrey}`,
        padding: theme.spacing(2),
    },
    cardImage: {
        width: "100%",
        height: "auto",
    },
    cardContent: {
        display: "flex",
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
    },
    cardText: {
        color: PaletteV1.NavBarBackgroundColor,
        fontWeight: "bold",
    },
    cardIcon: {
        color: PaletteV1.PrimaryColorMain,
    },
    imageCardTextContainer: {
        width: "100%",
        backgroundColor: PaletteV1.BackgroundColorLight,
        borderLeft: `2px solid ${PaletteV1.BorderColorGrey}`,
        borderBottom: `2px solid ${PaletteV1.BorderColorGrey}`,
        borderTop: `2px solid ${PaletteV1.BorderColorGrey}`,
        borderRight: `2px solid ${PaletteV1.BorderColorGrey}`,
        borderBottomLeftRadius: 12,
        borderBottomRightRadius: 12,
        padding: theme.spacing(2),
    },
}));
