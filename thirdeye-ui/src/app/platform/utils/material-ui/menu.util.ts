import { MenuProps } from "@material-ui/core";
import { DimensionV1 } from "./dimension.util";
import { typographyOptionsV1 } from "./typography.util";

// Material UI theme style overrides for Menu
export const menuItemClassesV1 = {
    root: {
        ...typographyOptionsV1.body2,
    },
};

// Material UI theme property overrides for Menu
export const menuPropsV1: Partial<MenuProps> = {
    anchorOrigin: {
        horizontal: "left",
        vertical: "bottom",
    },
    elevation: DimensionV1.PopoverElevation,
    getContentAnchorEl: null,
};
