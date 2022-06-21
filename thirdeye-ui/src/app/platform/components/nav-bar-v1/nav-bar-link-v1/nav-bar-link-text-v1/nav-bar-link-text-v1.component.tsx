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
import { ListItemText } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent, useEffect } from "react";
import { useNavBarV1 } from "../../nav-bar-v1/nav-bar-v1.component";
import { useNavBarLinkV1 } from "../nav-bar-link-v1/nav-bar-link-v1.component";
import { NavBarLinkTextV1Props } from "./nav-bar-link-text-v1.interfaces";
import { useNavBarLinkTextV1Styles } from "./nav-bar-link-text-v1.styles";

export const NavBarLinkTextV1: FunctionComponent<NavBarLinkTextV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const navBarLinkTextV1Classes = useNavBarLinkTextV1Styles();
    const { navBarMinimized } = useNavBarV1();
    const { hover, selected, setTooltip } = useNavBarLinkV1();

    useEffect(() => {
        // Set children as tooltip
        setTooltip(children);
    }, [children]);

    return (
        <>
            {!navBarMinimized && (
                // Visible only when nav bar is maximized
                <ListItemText
                    {...otherProps}
                    className={classNames(
                        hover || selected
                            ? navBarLinkTextV1Classes.navBarLinkTextHover
                            : navBarLinkTextV1Classes.navBarLinkTextRegular,
                        className,
                        "nav-bar-link-text-v1"
                    )}
                    primary={children}
                    primaryTypographyProps={{
                        variant: hover || selected ? "subtitle2" : "body2",
                    }}
                />
            )}
        </>
    );
};
