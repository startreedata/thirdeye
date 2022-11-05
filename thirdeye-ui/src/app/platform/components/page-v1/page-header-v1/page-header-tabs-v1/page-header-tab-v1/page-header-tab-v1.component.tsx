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
import { Tab } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent, useEffect } from "react";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import { usePageV1 } from "../../../page-v1/page-v1.component";
import { PageHeaderTabV1Props } from "./page-header-tab-v1.interfaces";

export const PageHeaderTabV1: FunctionComponent<PageHeaderTabV1Props> = ({
    href,
    selected,
    value,
    disabled,
    className,
    children,
    ...otherProps
}) => {
    const { headerVisible, setCurrentHeaderTab } = usePageV1();

    useEffect(() => {
        if (!selected) {
            return;
        }

        // Set children as current header tab
        setCurrentHeaderTab(children);
    }, [selected, children]);

    return (
        <>
            {headerVisible && (
                // Visible only when header is visible
                <Tab
                    {...otherProps}
                    className={classNames(className, "page-header-tab-v1")}
                    component={RouterLink}
                    disabled={disabled}
                    label={children}
                    selected={selected}
                    to={href}
                    value={value}
                />
            )}
        </>
    );
};
