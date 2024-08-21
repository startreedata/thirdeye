/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import React, { FunctionComponent } from "react";

// Components
import { ParentSize } from "@visx/responsive";
import BarGraphInternalComponent from "./bar-graph-internal.component";

// Interfaces
import { BarGraphProps } from "./bar-graph.interfaces";
import { BarLegend } from "./legend.component";
import { bargraphStyles } from "./bar-graph.styles";

const CHART_MARGINS = {
    top: 20,
    left: 50,
    bottom: 20,
    right: 50,
};

export const BarGraph: FunctionComponent<BarGraphProps> = (props) => {
    const componentStyles = bargraphStyles();

    return (
        <div className={componentStyles.container}>
            <ParentSize>
                {({ width, height }) => (
                    <BarGraphInternalComponent
                        height={props.height || height}
                        margins={CHART_MARGINS}
                        width={width}
                        {...props}
                    />
                )}
            </ParentSize>
            <div>
                <BarLegend labels={props.graphLegend} />
            </div>
        </div>
    );
};
