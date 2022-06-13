// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { HTMLAttributeAnchorTarget, ReactNode } from "react";

export interface PageHeaderNavLinkV1Props {
    href: string;
    externalLink?: boolean;
    target?: HTMLAttributeAnchorTarget;
    className?: string;
    children?: ReactNode;
}
