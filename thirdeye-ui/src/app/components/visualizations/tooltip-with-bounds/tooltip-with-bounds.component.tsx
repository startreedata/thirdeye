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
