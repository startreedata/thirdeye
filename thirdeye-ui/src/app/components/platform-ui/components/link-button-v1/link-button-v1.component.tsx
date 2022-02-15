// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Button } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import { LinkButtonV1Props } from "./link-button-v1.interfaces";

export const LinkButtonV1: FunctionComponent<LinkButtonV1Props> = ({
    href,
    externalLink,
    target,
    color,
    disabled,
    className,
    children,
    ...otherProps
}) => {
    return (
        <>
            {externalLink && (
                // Button as a link
                <Button
                    {...otherProps}
                    className={classNames(className, "link-button-v1")}
                    color={color}
                    disabled={disabled}
                    href={href}
                    target={target}
                >
                    {children}
                </Button>
            )}

            {!externalLink && (
                // Button as a router link
                <Button
                    {...otherProps}
                    className={classNames(className, "link-button-v1")}
                    color={color}
                    component={RouterLink}
                    disabled={disabled}
                    target={target}
                    to={href}
                >
                    {children}
                </Button>
            )}
        </>
    );
};
