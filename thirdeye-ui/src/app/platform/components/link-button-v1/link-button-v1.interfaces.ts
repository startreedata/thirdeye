// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
