import { MenuProps } from "@material-ui/core";
import { Dimension } from "./dimension.util";

// Material-UI theme style overrides for Menu
export const menuClasses = {
    paper: {
        minWidth: Dimension.MIN_WIDTH_MENU,
    },
};

// Material-UI theme property overrides for Menu
export const menuProps: Partial<MenuProps> = {
    anchorOrigin: {
        horizontal: "center",
        vertical: "bottom",
    },
    getContentAnchorEl: null,
};
