// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
}: PageHeaderTabV1Props) => {
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
