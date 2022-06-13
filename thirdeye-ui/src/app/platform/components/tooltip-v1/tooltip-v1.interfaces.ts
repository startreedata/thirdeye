// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactElement, ReactNode } from "react";

export interface TooltipV1Props {
    title: ReactNode;
    placement?:
        | "top"
        | "top-start"
        | "top-end"
        | "bottom"
        | "bottom-start"
        | "bottom-end"
        | "left"
        | "left-start"
        | "left-end"
        | "right"
        | "right-start"
        | "right-end";
    visible?: boolean;
    delay?: number;
    className?: string;
    children?: ReactElement;
}
