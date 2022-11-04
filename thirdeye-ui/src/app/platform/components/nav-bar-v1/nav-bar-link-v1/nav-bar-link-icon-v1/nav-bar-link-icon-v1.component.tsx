// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
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
