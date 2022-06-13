import { Dimension } from "./dimension.util";
import { Palette } from "./palette.util";

// Material-UI theme borders
export const Border = {
    BORDER_DEFAULT: `${Dimension.WIDTH_BORDER_DEFAULT}px solid ${Palette.COLOR_BORDER_DEFAULT}`,
    BORDER_INPUT_DEFAULT: `${Dimension.WIDTH_BORDER_DEFAULT}px solid ${Palette.COLOR_BORDER_INPUT_DEFAULT}`,
    BORDER_INPUT_ERROR: `${Dimension.WIDTH_BORDER_DEFAULT}px solid ${Palette.COLOR_BORDER_INPUT_ERROR}`,
} as const;
