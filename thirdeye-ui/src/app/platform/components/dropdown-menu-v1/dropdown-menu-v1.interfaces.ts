// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export interface DropdownMenuV1Props {
    dropdownMenuItems: DropdownMenuItemV1[];
    anchorEl?: null | Element | ((element: Element) => Element);
    open: boolean;
    className?: string;
    onClose?: () => void;
    onClick?: (menuItemId: number | string, text: string) => void;
}

export interface DropdownMenuItemV1 {
    id: number | string;
    text: string;
}
