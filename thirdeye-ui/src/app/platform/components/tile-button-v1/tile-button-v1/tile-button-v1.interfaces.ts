// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
