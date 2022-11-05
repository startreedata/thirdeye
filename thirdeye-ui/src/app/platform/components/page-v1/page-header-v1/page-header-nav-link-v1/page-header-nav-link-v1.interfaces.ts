import { HTMLAttributeAnchorTarget, ReactNode } from "react";

export interface PageHeaderNavLinkV1Props {
    href: string;
    externalLink?: boolean;
    target?: HTMLAttributeAnchorTarget;
    className?: string;
    children?: ReactNode;
}
