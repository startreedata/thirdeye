// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageHeaderActionsV1Props } from "./page-header-actions-v1.interfaces";
import { usePageHeaderActionsV1Styles } from "./page-header-actions-v1.styles";

export const PageHeaderActionsV1: FunctionComponent<PageHeaderActionsV1Props> = ({
    className,
    children,
    ...otherProps
}: PageHeaderActionsV1Props) => {
    const pageHeaderActionsV1Classes = usePageHeaderActionsV1Styles();

    return (
        <div
            {...otherProps}
            className={classNames(
                pageHeaderActionsV1Classes.pageHeaderActions,
                className,
                "page-header-actions-v1"
            )}
        >
            {children}
        </div>
    );
};
