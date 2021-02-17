import { WithWidth } from "@material-ui/core";
import { ScaleTime } from "d3-scale";

export interface TimeAxisBottomProps extends WithWidth {
    top?: number;
    left?: number;
    numTicks?: number;
    scale: ScaleTime<number, number>;
}
