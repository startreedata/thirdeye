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
import { Button } from "@material-ui/core";
import KeyboardArrowDownIcon from "@material-ui/icons/KeyboardArrowDown";
import MoreHorizIcon from "@material-ui/icons/MoreHoriz";
import classNames from "classnames";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { DropdownMenuV1 } from "../dropdown-menu-v1";
import { SquareSvgIconButtonV1 } from "../square-svg-icon-button-v1";
import {
    DropdownButtonTypeV1,
    DropdownButtonV1Props,
} from "./dropdown-button-v1.interfaces";

export const DropdownButtonV1: FunctionComponent<DropdownButtonV1Props> = ({
    dropdownMenuItems,
    type,
    color,
    disabled,
    className,
    onClick,
    children,
    ...otherProps
}) => {
    const [menuElement, setMenuElement] = useState<HTMLElement | null>();

    const handleButtonClick = (event: MouseEvent<HTMLElement>): void => {
        setMenuElement(event.currentTarget);
    };

    const handleDropdownClose = (): void => {
        setMenuElement(null);
    };

    const handleMenuItemClick = (
        menuItemId: number | string,
        text: string
    ): void => {
        onClick && onClick(menuItemId, text);
        handleDropdownClose();
    };

    return (
        <>
            {type === DropdownButtonTypeV1.MoreOptions && (
                // Dropdown button as more options square button
                <SquareSvgIconButtonV1
                    {...otherProps}
                    className={classNames(className, "dropdown-button-v1")}
                    color={color}
                    disabled={disabled}
                    svgIcon={MoreHorizIcon}
                    onClick={handleButtonClick}
                />
            )}

            {type !== DropdownButtonTypeV1.MoreOptions && (
                // Dropdown button as a regular button with dropdown end icon
                <Button
                    {...otherProps}
                    className={classNames(className, "dropdown-button-v1")}
                    color={color}
                    disabled={disabled}
                    endIcon={<KeyboardArrowDownIcon />}
                    onClick={handleButtonClick}
                >
                    {children}
                </Button>
            )}

            {/* Menu */}
            <DropdownMenuV1
                anchorEl={menuElement}
                className="dropdown-button-v1-menu"
                dropdownMenuItems={dropdownMenuItems}
                open={Boolean(menuElement)}
                onClick={handleMenuItemClick}
                onClose={handleDropdownClose}
            />
        </>
    );
};
