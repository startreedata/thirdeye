import { ScaleLinear } from "d3-scale";

export interface LinearAxisLeftProps {
    top?: number;
    left?: number;
    numTicks?: number;
    scale: ScaleLinear<number, number>;
}
