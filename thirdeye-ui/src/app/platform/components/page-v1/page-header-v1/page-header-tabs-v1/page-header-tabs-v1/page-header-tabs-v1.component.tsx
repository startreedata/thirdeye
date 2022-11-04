// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Tabs } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageHeaderTabsV1Props } from "./page-header-tabs-v1.interfaces";
import { usePageHeaderTabsV1Styles } from "./page-header-tabs-v1.styles";

export const PageHeaderTabsV1: FunctionComponent<PageHeaderTabsV1Props> = ({
    selectedIndex,
    className,
    children,
    ...otherProps
}) => {
    const pageHeaderTabsV1Classes = usePageHeaderTabsV1Styles();

    return (
        <Tabs
            {...otherProps}
            className={classNames(
                pageHeaderTabsV1Classes.pageHeaderTabs,
                className,
                "page-header-tabs-v1"
            )}
            value={selectedIndex}
        >
            {children}
        </Tabs>
    );
};
