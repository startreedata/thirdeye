// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";
import { BorderV1 } from "../../../../utils/material-ui/border.util";

export const usePageContentsCardV1Styles = makeStyles({
    pageContentsCard: {
        border: BorderV1.BorderDefault,
    },
    pageContentsCardFullHeight: {
        height: "100%",
    },
    pageContentsCardPaddingDisabled: {
        padding: 0,
        "&:last-child": {
            paddingBottom: 0,
        },
    },
});
