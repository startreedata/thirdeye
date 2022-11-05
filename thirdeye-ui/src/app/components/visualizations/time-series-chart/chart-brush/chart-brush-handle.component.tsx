import { BrushHandleRenderProps } from "@visx/brush/lib/BrushHandle";
import { Group } from "@visx/group";
import React, { FunctionComponent } from "react";

export const BrushHandle: FunctionComponent<BrushHandleRenderProps> = ({
    x,
    height,
    isBrushActive,
}) => {
    const pathWidth = 8;
    const pathHeight = 15;

    if (!isBrushActive) {
        return null;
    }

    return (
        <Group left={x + pathWidth / 2} top={(height - pathHeight) / 2}>
            <path
                d="M -4.5 0.5 L 3.5 0.5 L 3.5 15.5 L -4.5 15.5 L -4.5 0.5 M -1.5 4 L -1.5 12 M 0.5 4 L 0.5 12"
                fill="#f2f2f2"
                stroke="#999999"
                strokeWidth="1"
                style={{ cursor: "ew-resize" }}
            />
        </Group>
    );
};
