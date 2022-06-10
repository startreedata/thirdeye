///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";
import { BorderV1 } from "../../../utils/material-ui/border.util";
import { DimensionV1 } from "../../../utils/material-ui/dimension.util";
import { PaletteV1 } from "../../../utils/material-ui/palette.util";
import {
    HEIGHT_NAV_BAR_LINK,
    PADDING_NAV_BAR_LINK,
} from "../nav-bar-link-v1/nav-bar-link-v1/nav-bar-link-v1.styles";

export const useNavBarV1Styles = makeStyles((theme) => ({
    navBar: {
        display: "flex",
        backgroundColor: PaletteV1.NavBarBackgroundColor,
        whiteSpace: "nowrap",
        overflowX: "hidden",
    },
    navBarMinimized: {
        width: DimensionV1.NavBarWidthMinimized,
        transition: theme.transitions.create("width", {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    navBarMaximized: {
        width: DimensionV1.NavBarWidthMaximized,
        transition: theme.transitions.create("width", {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
    navBarHeaderContainer: {
        position: "sticky",
        top: 0,
        // Set height to page header height sans one nav bar link and the dividing border
        minHeight:
            DimensionV1.PageHeaderHeight -
            HEIGHT_NAV_BAR_LINK -
            DimensionV1.BorderWidthDefault,
        maxHeight:
            DimensionV1.PageHeaderHeight -
            HEIGHT_NAV_BAR_LINK -
            DimensionV1.BorderWidthDefault,
        backgroundColor: "inherit",
        borderBottom: BorderV1.BorderSecondary,
        zIndex: "inherit", // Always above nav bar contents
    },
    navBarHeader: {
        height: "100%",
        display: "flex",
        alignItems: "center",
        paddingLeft: PADDING_NAV_BAR_LINK,
    },
    navBarHeaderMinimized: {
        width: DimensionV1.NavBarWidthMinimized,
    },
    navBarHeaderMaximized: {
        width: DimensionV1.NavBarWidthMaximized,
    },
    navBarHeaderLogo: {
        height: 40,
    },
    navBarLinks: {
        display: "flex",
        flex: 1,
        flexDirection: "column",
        justifyContent: "flex-end",
    },
}));
