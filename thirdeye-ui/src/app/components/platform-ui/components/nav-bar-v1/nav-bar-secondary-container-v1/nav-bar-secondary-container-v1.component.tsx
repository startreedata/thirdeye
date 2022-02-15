// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { List } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { NavBarSecondaryContainerV1Props } from "./nav-bar-secondary-container-v1.interfaces";
import { useNavBarSecondaryContainerV1Styles } from "./nav-bar-secondary-container-v1.styles";

export const NavBarSecondaryContainerV1: FunctionComponent<NavBarSecondaryContainerV1Props> = ({
    className,
    children,
    ...otherProps
}: NavBarSecondaryContainerV1Props) => {
    const navBarSecondaryContainerV1Classes = useNavBarSecondaryContainerV1Styles();

    return (
        <List
            {...otherProps}
            disablePadding
            className={classNames(
                navBarSecondaryContainerV1Classes.navBarSecondaryContainer,
                className,
                "nav-bar-secondary-container-v1"
            )}
            component="nav"
        >
            {children}
        </List>
    );
};
