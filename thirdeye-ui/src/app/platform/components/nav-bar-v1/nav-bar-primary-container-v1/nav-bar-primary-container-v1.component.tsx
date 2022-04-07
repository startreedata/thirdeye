// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { List } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { NavBarPrimaryContainerV1Props } from "./nav-bar-primary-container-v1.interfaces";
import { useNavBarPrimaryContainerV1Styles } from "./nav-bar-primary-container-v1.styles";

export const NavBarPrimaryContainerV1: FunctionComponent<
    NavBarPrimaryContainerV1Props
> = ({ className, children, ...otherProps }) => {
    const navBarPrimaryContainerV1Classes = useNavBarPrimaryContainerV1Styles();

    return (
        <List
            {...otherProps}
            disablePadding
            className={classNames(
                navBarPrimaryContainerV1Classes.navBarPrimaryContainer,
                className,
                "nav-bar-primary-container-v1"
            )}
            component="nav"
        >
            {children}
        </List>
    );
};
