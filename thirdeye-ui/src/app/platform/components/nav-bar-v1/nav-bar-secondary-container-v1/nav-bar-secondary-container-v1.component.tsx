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
// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { List } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { NavBarSecondaryContainerV1Props } from "./nav-bar-secondary-container-v1.interfaces";
import { useNavBarSecondaryContainerV1Styles } from "./nav-bar-secondary-container-v1.styles";

export const NavBarSecondaryContainerV1: FunctionComponent<
    NavBarSecondaryContainerV1Props
> = ({ className, children, ...otherProps }) => {
    const navBarSecondaryContainerV1Classes =
        useNavBarSecondaryContainerV1Styles();

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
