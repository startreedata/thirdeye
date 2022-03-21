import { GridProps } from "@material-ui/core";
import { Dimension } from "./dimension.util";

// Material-UI theme property overrides for Grid
export const gridProps: Partial<GridProps> = {
    spacing: Dimension.SPACING_GRID_DEFAULT,
};
