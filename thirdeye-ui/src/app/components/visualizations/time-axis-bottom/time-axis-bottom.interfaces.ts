import { ScaleTime } from "d3-scale";

export interface TimeAxisBottomProps {
    top?: number;
    left?: number;
    numTicks?: number;
    scale: ScaleTime<number, number>;
}
