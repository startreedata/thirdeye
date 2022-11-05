import { HTMLAttributeAnchorTarget, MouseEventHandler, ReactNode } from "react";

export interface TileButtonV1Props {
    href?: string;
    externalLink?: boolean;
    target?: HTMLAttributeAnchorTarget;
    disabled?: boolean;
    className?: string;
    onClick?: MouseEventHandler;
    children?: ReactNode;
}

export interface TileButtonV1ContextProps {
    disabled: boolean;
}
