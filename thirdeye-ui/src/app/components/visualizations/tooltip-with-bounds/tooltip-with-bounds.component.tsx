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
import { TooltipWithBounds as VisxTooltipWithBounds } from "@visx/tooltip";
import React, { FunctionComponent } from "react";
import { TooltipWithBoundsProps } from "./tooltip-with-bounds.interfaces";
import { useTooltipWithBoundsStyles } from "./tooltip-with-bounds.styles";

export const TooltipWithBounds: FunctionComponent<TooltipWithBoundsProps> = (
    props: TooltipWithBoundsProps
) => {
    const tooltipWithBoundsClasses = useTooltipWithBoundsStyles();

    return (
        <div className={tooltipWithBoundsClasses.tooltipContainer}>
            {props.children}

            {/* Tooltip */}
            {props.open && (
                <VisxTooltipWithBounds
                    className={tooltipWithBoundsClasses.tooltip}
                    left={props.left}
                    top={props.top}
                >
                    {props.title}
                </VisxTooltipWithBounds>
            )}
        </div>
    );
};
