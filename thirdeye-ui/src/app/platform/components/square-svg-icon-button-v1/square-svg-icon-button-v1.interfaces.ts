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
