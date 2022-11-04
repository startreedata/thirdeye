import { Variant } from "@material-ui/core/styles/createTypography";
import { HTMLAttributeAnchorTarget, MouseEventHandler, ReactNode } from "react";

export interface LinkV1Props {
    href?: string;
    externalLink?: boolean;
    target?: HTMLAttributeAnchorTarget;
    disabled?: boolean;
    noWrap?: boolean;
    color?:
        | "initial"
        | "inherit"
        | "primary"
        | "secondary"
        | "textPrimary"
        | "textSecondary"
        | "error";
    variant: Variant; // Required as when rendering as a button, variant doesn't seem to be inherited appropriately
    className?: string;
    onClick?: MouseEventHandler;
    children?: ReactNode;
}
