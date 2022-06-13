// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { PropTypes, SvgIcon } from "@material-ui/core";
import { HTMLAttributeAnchorTarget, MouseEventHandler } from "react";

export interface SquareSvgIconButtonV1Props {
    href?: string;
    externalLink?: boolean;
    target?: HTMLAttributeAnchorTarget;
    color?: PropTypes.Color;
    disabled?: boolean;
    svgIcon: typeof SvgIcon;
    className?: string;
    onClick?: MouseEventHandler;
}
