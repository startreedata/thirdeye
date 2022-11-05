/*
 * Copyright 2022 StarTree Inc
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
import { DimensionV1 } from "../../../../utils/material-ui/dimension.util";
import { PADDING_NAV_BAR_LINK } from "../nav-bar-link-v1/nav-bar-link-v1.styles";

export const useNavBarLinkIconV1Styles = makeStyles((theme) => ({
    navBarLinkIcon: {
        // Set width to minimized nav bar width sans left padding
        // (right padding needed between icon and text)
        minWidth: DimensionV1.NavBarWidthMinimized - PADDING_NAV_BAR_LINK,
        maxWidth: DimensionV1.NavBarWidthMinimized - PADDING_NAV_BAR_LINK,
        "& svg": {
            width: DimensionV1.NavBarWidthMinimized - PADDING_NAV_BAR_LINK * 2, // For SVGs, width of minimized nav bar sans left and right padding
            fontSize: "x-large", // For Material UI SVGs
        },
    },
    navBarLinkIconRegular: {
        "& svg": {
            color: theme.palette.secondary.main,
            fill: theme.palette.secondary.main,
        },
    },
    navBarLinkIconHover: {
        "& svg": {
            color: theme.palette.primary.contrastText,
            fill: theme.palette.primary.contrastText,
        },
    },
}));
