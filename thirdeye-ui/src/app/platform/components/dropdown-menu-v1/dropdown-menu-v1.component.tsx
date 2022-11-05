import { Menu, MenuItem } from "@material-ui/core";
import classNames from "classnames";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import {
    DropdownMenuItemV1,
    DropdownMenuV1Props,
} from "./dropdown-menu-v1.interfaces";

export const DropdownMenuV1: FunctionComponent<DropdownMenuV1Props> = ({
    dropdownMenuItems,
    anchorEl,
    open,
    className,
    onClose,
    onClick,
    ...otherProps
}) => {
    const handleMenuItemClick = (menuItem: DropdownMenuItemV1): void => {
        onClick && onClick(menuItem.id, menuItem.text);
    };

    return (
        <>
            {!isEmpty(dropdownMenuItems) && (
                <Menu
                    {...otherProps}
                    anchorEl={anchorEl}
                    className={classNames(className, "dropdown-menu-v1")}
                    open={open}
                    onClose={onClose}
                >
                    {dropdownMenuItems.map((eachDropdownMenuItem, index) => (
                        <MenuItem
                            className="dropdown-menu-v1-item"
                            key={index}
                            onClick={() =>
                                handleMenuItemClick(eachDropdownMenuItem)
                            }
                        >
                            {eachDropdownMenuItem.text}
                        </MenuItem>
                    ))}
                </Menu>
            )}
        </>
    );
};
