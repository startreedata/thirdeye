import {
    AxisLeft,
    AxisRight,
    Orientation,
    TickRendererProps,
} from "@visx/axis";
import { Text } from "@visx/text";
import React, { FunctionComponent, ReactNode, useMemo } from "react";
import { formatLargeNumberForVisualization } from "../../../utils/visualization/visualization.util";
import { LinearAxisYProps } from "./linear-axis-y.interfaces";
import { useLinearAxisYStyles } from "./linear-axis-y.styles";

// Customization of visx linear axis with formatted tick labels
export const LinearAxisY: FunctionComponent<LinearAxisYProps> = (
    props: LinearAxisYProps
) => {
    const linearAxisYClasses = useLinearAxisYStyles();

    // Renders formatted label value from tick renderer props without any visx label font properties
    const tickComponentRenderer = (
        tickRendererProps: TickRendererProps
    ): ReactNode => {
        console.log(tickRendererProps.textAnchor);

        return (
            <Text
                textAnchor={tickRendererProps.textAnchor}
                x={
                    tickRendererProps.x +
                    (props.orientation === Orientation.right ? 2 : -2)
                }
                y={tickRendererProps.y + 4}
            >
                {tickRendererProps.formattedValue}
            </Text>
        );
    };

    // Determine wether it's Left or Right axis
    // Default it will be the left oriented axis
    const Axis = useMemo(
        () => (props.orientation === Orientation.right ? AxisRight : AxisLeft),
        [props.orientation]
    );

    return (
        <Axis
            left={props.left}
            numTicks={props.numTicks}
            orientation={props.orientation}
            scale={props.scale}
            tickClassName={linearAxisYClasses.tick}
            tickComponent={tickComponentRenderer}
            tickFormat={formatLargeNumberForVisualization}
            top={props.top}
        />
    );
};
