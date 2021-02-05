import { ScaleLinear } from "d3-scale";

export interface LinearAxisLeftProps {
    left?: number;
    top?: number;
    numTicks?: number;
    scale: ScaleLinear<number, number>;
}
