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
import { Toolbar } from "@material-ui/core";
import ChevronLeftIcon from "@material-ui/icons/ChevronLeft";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { LinkV1 } from "../../../link-v1/link-v1.component";
import { usePageV1 } from "../../page-v1/page-v1.component";
import { PageHeaderNavLinkV1Props } from "./page-header-nav-link-v1.interfaces";
import { usePageHeaderNavLinkV1Styles } from "./page-header-nav-link-v1.styles";

export const PageHeaderNavLinkV1: FunctionComponent<
    PageHeaderNavLinkV1Props
> = ({ href, externalLink, target, className, children, ...otherProps }) => {
    const pageHeaderNavLinkV1Classes = usePageHeaderNavLinkV1Styles();
    const { headerVisible } = usePageV1();

    return (
        <>
            {headerVisible && (
                // Visible only when header is visible
                <Toolbar
                    {...otherProps}
                    disableGutters
                    className={classNames(
                        pageHeaderNavLinkV1Classes.pageHeaderNavLink,
                        className,
                        "page-header-nav-link-v1"
                    )}
                    variant="dense"
                >
                    <LinkV1
                        noWrap
                        className="page-header-nav-link-v1-link"
                        color="textPrimary"
                        externalLink={externalLink}
                        href={href}
                        target={target}
                        variant="subtitle2"
                    >
                        {/* Icon */}
                        <ChevronLeftIcon
                            className={
                                pageHeaderNavLinkV1Classes.pageHeaderNavLinkIcon
                            }
                            color="action"
                            fontSize="small"
                        />

                        {children}
                    </LinkV1>
                </Toolbar>
            )}
        </>
    );
};
