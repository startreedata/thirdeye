// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { HTMLAttributeAnchorTarget, MouseEventHandler, ReactNode } from "react";

export interface NavBarLinkV1Props {
    href?: string;
    externalLink?: boolean;
    target?: HTMLAttributeAnchorTarget;
    selected?: boolean;
    className?: string;
    onClick?: MouseEventHandler;
    children?: ReactNode;
}

export interface NavBarLinkV1ContextProps {
    hover: boolean;
    selected: boolean;
    setTooltip: (tooltip: ReactNode) => void;
}
