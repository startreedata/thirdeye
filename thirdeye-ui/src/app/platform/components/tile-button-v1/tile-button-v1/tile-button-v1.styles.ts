// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";
import { DimensionV1 } from "../../../utils/material-ui/dimension.util";

export const useTileButtonV1Styles = makeStyles((theme) => ({
    tileButton: {
        height: 200,
        width: 240,
        backgroundColor: theme.palette.background.paper,
        borderRadius: DimensionV1.CardBorderRadius,
        padding: theme.spacing(3),
    },
    tileButtonLabel: {
        height: "100%",
        width: "100%",
        display: "flex",
        flexDirection: "column",
        textTransform: "none",
    },
}));
