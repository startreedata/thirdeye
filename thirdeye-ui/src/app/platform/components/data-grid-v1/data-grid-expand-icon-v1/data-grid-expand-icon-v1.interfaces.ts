// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export interface DataGridExpandIconV1Props {
    depth: number;
    expandable: boolean;
    expanded: boolean;
    className?: string;
    onExpand: (expanded: boolean) => void;
    [key: string]: unknown;
}
