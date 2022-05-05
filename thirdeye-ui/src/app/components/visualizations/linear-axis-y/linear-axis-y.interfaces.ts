import { Orientation } from "@visx/axis";
import { ScaleLinear } from "d3-scale";

export interface LinearAxisYProps {
    top?: number;
    left?: number;
    numTicks?: number;
    scale: ScaleLinear<number, number>;
    orientation?: Orientation;
}
