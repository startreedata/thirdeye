// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactNode } from "react";

export interface PageHeaderTabV1Props {
    href: string;
    selected?: boolean;
    value?: number | string;
    disabled?: boolean;
    className?: string;
    children?: ReactNode;
}
