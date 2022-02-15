// Copyright 2022 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";

export const useHelpLinkIconV1Styles = makeStyles((theme) => ({
    helpIconFlex: {
        display: "flex",
    },
    helpIconInline: {
        display: "inline-block",
    },
    helpIconPadding: {
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
    },
}));
