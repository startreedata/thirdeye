import { AxisLeft, TickRendererProps } from "@visx/axis";
import { Text } from "@visx/text";
import React, { FunctionComponent, ReactNode } from "react";
import { formatLargeNumberForVisualization } from "../../../utils/visualization/visualization.util";
import { LinearAxisLeftProps } from "./linear-axis-left.interfaces";
import { useLinearAxisLeftStyles } from "./linear-axis-left.styles";

// Customization of visx linear axis with formatted tick labels
export const LinearAxisLeft: FunctionComponent<LinearAxisLeftProps> = (
    props: LinearAxisLeftProps
) => {
    const linearAxisLeftClasses = useLinearAxisLeftStyles();

    // Renders formatted label value from tick renderer props without any visx label font properties
    const tickComponentRenderer = (
        tickRendererProps: TickRendererProps
    ): ReactNode => {
        return (
            <Text
                textAnchor={tickRendererProps.textAnchor}
                x={tickRendererProps.x - 2}
                y={tickRendererProps.y + 4}
            >
                {tickRendererProps.formattedValue}
            </Text>
        );
    };

    return (
        <AxisLeft
            left={props.left}
            numTicks={props.numTicks}
            scale={props.scale}
            tickClassName={linearAxisLeftClasses.tick}
            tickComponent={tickComponentRenderer}
            tickFormat={formatLargeNumberForVisualization}
            top={props.top}
        />
    );
};
