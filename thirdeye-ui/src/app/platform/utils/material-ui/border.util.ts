import { DimensionV1 } from "./dimension.util";
import { PaletteV1 } from "./palette.util";

// Material UI theme borders
export const BorderV1 = {
    TabBorderSelected: `${DimensionV1.TabBorderWidthSelected}px solid`,
    BorderDefault: `${DimensionV1.BorderWidthDefault}px solid ${PaletteV1.BorderColorDefault}`,
    BorderSecondary: `${DimensionV1.BorderWidthDefault}px solid ${PaletteV1.SecondaryColorMain}`,
} as const;
