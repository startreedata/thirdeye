// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import Tippy from "@tippyjs/react";
import classNames from "classnames";
import { isNil } from "lodash";
import React, { FunctionComponent } from "react";
import "tippy.js/animations/shift-away-extreme.css";
import "tippy.js/dist/tippy.css";
import { TooltipV1Props } from "./tooltip-v1.interfaces";
import { useTooltipV1Styles } from "./tooltip-v1.styles";

const DELAY_DEFAULT = 600;

export const TooltipV1: FunctionComponent<TooltipV1Props> = ({
    title,
    placement = "bottom",
    visible,
    delay = DELAY_DEFAULT,
    className,
    children,
    ...otherProps
}: TooltipV1Props) => {
    const tooltipV1Classes = useTooltipV1Styles();

    return (
        <Tippy
            {...otherProps}
            animation="shift-away-extreme"
            arrow={false}
            className={classNames(
                tooltipV1Classes.tooltip,
                className,
                "tooltip-v1"
            )}
            content={title}
            delay={[delay, null]}
            disabled={isNil(visible) ? false : !visible}
            offset={[0, 8]}
            placement={placement}
        >
            {children}
        </Tippy>
    );
};
