// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
        },
    },
    navBarLinkIconHover: {
        "& svg": {
            color: theme.palette.primary.contrastText,
        },
    },
}));
