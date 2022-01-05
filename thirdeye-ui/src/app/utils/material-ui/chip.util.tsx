import { ChipProps, IconButton } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import React from "react";
import { Border } from "./border.util";
import { Dimension } from "./dimension.util";

// Material UI theme style overrides for Chip
export const chipClasses = {
    root: {
        borderRadius: Dimension.RADIUS_BORDER_DEFAULT,
    },
    outlined: {
        border: Border.BORDER_DEFAULT,
    },
};

// Material UI theme property overrides for Chip
export const chipProps: Partial<ChipProps> = {
    variant: "outlined",
    deleteIcon: (
        <IconButton size="small">
            <CloseIcon color="action" fontSize="small" />
        </IconButton>
    ),
    color: "secondary",
};
