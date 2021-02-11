import { Palette } from "./palette.util";
import { typographyOptions } from "./typography.util";

// Material-UI theme style overrides for Tooltip
export const tooltipClasses = {
    tooltip: {
        backgroundColor: Palette.COLOR_BACKGROUND_TOOLTIP,
        ...typographyOptions.body2,
    },
    arrow: {
        color: Palette.COLOR_BACKGROUND_TOOLTIP,
    },
};
