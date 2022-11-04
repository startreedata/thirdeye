import { PropTypes } from "@material-ui/core";
import { ReactNode } from "react";
import { DropdownMenuItemV1 } from "../dropdown-menu-v1/dropdown-menu-v1.interfaces";

export interface DropdownButtonV1Props {
    dropdownMenuItems: DropdownMenuItemV1[];
    type?: DropdownButtonTypeV1;
    color?: PropTypes.Color;
    disabled?: boolean;
    className?: string;
    onClick?: (menuItemId: number | string, text: string) => void;
    children?: ReactNode;
}

export enum DropdownButtonTypeV1 {
    Regular,
    MoreOptions,
    Custom,
}
