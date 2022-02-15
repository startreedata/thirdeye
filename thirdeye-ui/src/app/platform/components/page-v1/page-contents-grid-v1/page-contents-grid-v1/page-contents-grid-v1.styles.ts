// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";
import { DimensionV1 } from "../../../../utils/material-ui/dimension.util";

export const usePageContentsGridV1Styles = makeStyles((theme) => ({
    pageContentsGrid: {
        minWidth: 300,
        display: "flex",
        flexDirection: "column",
        paddingBottom: theme.spacing(DimensionV1.PageGridSpacing),
        paddingLeft: theme.spacing(DimensionV1.PageGridSpacing),
        paddingRight: theme.spacing(DimensionV1.PageGridSpacing),
    },
    pageContentsGridFullHeight: {
        flex: 1,
    },
}));
