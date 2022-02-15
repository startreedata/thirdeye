// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
