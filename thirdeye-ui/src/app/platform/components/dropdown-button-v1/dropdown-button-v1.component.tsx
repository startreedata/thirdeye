// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
