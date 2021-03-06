import { PopoverProps } from "@material-ui/core";

// Material-UI theme property overrides for Popover
export const popoverProps: Partial<PopoverProps> = {
    anchorOrigin: {
        horizontal: "center",
        vertical: "bottom",
    },
};
