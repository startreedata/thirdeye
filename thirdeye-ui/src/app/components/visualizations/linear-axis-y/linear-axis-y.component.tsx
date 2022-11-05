/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
