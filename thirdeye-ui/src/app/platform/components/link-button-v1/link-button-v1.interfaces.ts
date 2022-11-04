import { PropTypes } from "@material-ui/core";
import { HTMLAttributeAnchorTarget, ReactNode } from "react";

export interface LinkButtonV1Props {
    href: string;
    externalLink?: boolean;
    target?: HTMLAttributeAnchorTarget;
    color?: PropTypes.Color;
    disabled?: boolean;
    className?: string;
    children?: ReactNode;
}
