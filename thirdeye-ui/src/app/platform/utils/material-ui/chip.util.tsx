// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ChipProps, IconButton } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import React from "react";
import { BorderV1 } from "./border.util";
import { DimensionV1 } from "./dimension.util";

// Material UI theme style overrides for Chip
export const chipClassesV1 = {
    root: {
        borderRadius: DimensionV1.BorderRadiusDefault,
    },
    outlined: {
        border: BorderV1.BorderDefault,
    },
};

// Material UI theme property overrides for Chip
export const chipPropsV1: Partial<ChipProps> = {
    variant: "outlined",
    deleteIcon: (
        <IconButton size="small">
            <CloseIcon color="action" fontSize="small" />
        </IconButton>
    ),
};
