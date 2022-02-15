// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ListItemIcon } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { useNavBarLinkV1 } from "../nav-bar-link-v1/nav-bar-link-v1.component";
import { NavBarLinkIconV1Props } from "./nav-bar-link-icon-v1.interfaces";
import { useNavBarLinkIconV1Styles } from "./nav-bar-link-icon-v1.styles";

export const NavBarLinkIconV1: FunctionComponent<NavBarLinkIconV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const navBarLinkIconV1Classes = useNavBarLinkIconV1Styles();
    const { hover, selected } = useNavBarLinkV1();

    return (
        <ListItemIcon
            {...otherProps}
            className={classNames(
                navBarLinkIconV1Classes.navBarLinkIcon,
                hover || selected
                    ? navBarLinkIconV1Classes.navBarLinkIconHover
                    : navBarLinkIconV1Classes.navBarLinkIconRegular,
                className,
                "nav-bar-link-icon-v1"
            )}
        >
            {children}
        </ListItemIcon>
    );
};
