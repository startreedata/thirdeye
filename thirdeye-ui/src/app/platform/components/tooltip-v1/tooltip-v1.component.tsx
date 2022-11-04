// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
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
}) => {
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
