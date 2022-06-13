// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";
import { DimensionV1 } from "../../utils/material-ui/dimension.util";

export const useAutocompletePaperV1Styles = makeStyles({
    autocompletePaper: {
        borderRadius: DimensionV1.PopoverBorderRadius,
    },
});
