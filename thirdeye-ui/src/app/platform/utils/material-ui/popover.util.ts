import { PopoverProps } from "@material-ui/core";
import { DimensionV1 } from "./dimension.util";

// Material UI theme style overrides for Popover
export const popoverClassesV1 = {
    paper: {
        minWidth: DimensionV1.MenuWidth,
        borderRadius: DimensionV1.PopoverBorderRadius,
    },
};

// Material UI theme property overrides for Popover
export const popoverPropsV1: Partial<PopoverProps> = {
    anchorOrigin: {
        horizontal: "left",
        vertical: "bottom",
    },
    elevation: DimensionV1.PopoverElevation,
    getContentAnchorEl: null,
};
