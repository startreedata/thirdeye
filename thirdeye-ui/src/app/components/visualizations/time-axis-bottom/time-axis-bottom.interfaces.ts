import { ScaleTime } from "d3-scale";

export interface TimeAxisBottomProps {
    left?: number;
    top?: number;
    numTicks?: number;
    scale: ScaleTime<number, number>;
}
