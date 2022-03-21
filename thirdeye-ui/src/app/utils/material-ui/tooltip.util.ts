import { Palette } from "./palette.util";
import { typographyOptions } from "./typography.util";

// Material-UI theme style overrides for Tooltip
export const tooltipClasses = {
    arrow: {
        color: Palette.COLOR_BACKGROUND_TOOLTIP,
    },
    tooltip: {
        ...typographyOptions.body2,
        backgroundColor: Palette.COLOR_BACKGROUND_TOOLTIP,
    },
};
