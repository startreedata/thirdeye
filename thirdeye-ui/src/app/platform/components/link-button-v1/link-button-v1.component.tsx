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
