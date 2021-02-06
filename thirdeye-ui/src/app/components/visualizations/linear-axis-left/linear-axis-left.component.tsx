import { AxisLeft } from "@visx/visx";
import React, { FunctionComponent } from "react";
import { formatLargeNumberForVisualization } from "../../../utils/visualization/visualization.util";
import { LinearAxisLeftProps } from "./linear-axis-left.interfaces";
import { useLinearAxisLeftStyles } from "./linear-axis-left.styles";

export const LinearAxisLeft: FunctionComponent<LinearAxisLeftProps> = (
    props: LinearAxisLeftProps
) => {
    const linearAxisLeftClasses = useLinearAxisLeftStyles();

    return (
        <AxisLeft
            left={props.left}
            numTicks={props.numTicks}
            scale={props.scale}
            tickClassName={linearAxisLeftClasses.tick}
            tickFormat={formatLargeNumberForVisualization}
            top={props.top}
        />
    );
};
