import { ScaleTime } from "d3-scale";

export interface VisxCustomTimeAxisBottomProps {
    left?: number;
    top?: number;
    numTicks?: number;
    tickClassName: string;
    scale: ScaleTime<number, number>;
}
