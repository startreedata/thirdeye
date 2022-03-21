// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";

export const usePageHeaderActionsV1Styles = makeStyles((theme) => ({
    pageHeaderActions: {
        display: "flex",
        flex: 1,
        justifyContent: "flex-end",
        "&>*": {
            marginLeft: theme.spacing(1),
        },
    },
}));
