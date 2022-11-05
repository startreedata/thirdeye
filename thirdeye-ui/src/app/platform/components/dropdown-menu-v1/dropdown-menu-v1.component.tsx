/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
