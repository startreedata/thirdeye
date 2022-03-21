// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";

export const useTileButtonIconV1Styles = makeStyles({
    tileButtonIcon: {
        display: "flex",
        flex: 1,
        alignItems: "center",
        "& svg": {
            height: 75,
            width: 75,
        },
    },
    tileButtonIconEnabled: {
        opacity: 1,
    },
    tileButtonIconDisabled: {
        opacity: 0.4,
    },
});
