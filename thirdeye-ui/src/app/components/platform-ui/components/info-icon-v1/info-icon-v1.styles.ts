// Copyright 2022 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";
import { DimensionV1 } from "../..";

export const useInfoIconV1Styles = makeStyles((theme) => ({
    infoIconFlex: {
        display: "flex",
    },
    infoIconInline: {
        display: "inline-block",
    },
    infoIconPadding: {
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
    },
    info: {
        padding: theme.spacing(1),
    },
    autoFitToContents: {
        minWidth: "auto",
    },
    defaultSizing: {
        maxWidth: DimensionV1.MenuWidth * 2,
    },
}));
