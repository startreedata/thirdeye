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
import { Typography } from "@material-ui/core";
import { Skeleton } from "@material-ui/lab";
import classNames from "classnames";
import React, { FunctionComponent, useEffect } from "react";
import { usePageV1 } from "../../page-v1/page-v1.component";
import { PageHeaderTextV1Props } from "./page-header-text-v1.interfaces";
import { usePageHeaderTextV1Styles } from "./page-header-text-v1.styles";

export const PageHeaderTextV1: FunctionComponent<PageHeaderTextV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const pageHeaderTextV1Classes = usePageHeaderTextV1Styles();
    const { headerVisible, setHeaderText } = usePageV1();

    useEffect(() => {
        // Set children as header text
        setHeaderText(children);
    }, [children]);

    return (
        <>
            {headerVisible && (
                // Visible only when header is visible
                <Typography
                    {...otherProps}
                    noWrap
                    className={classNames(
                        {
                            [pageHeaderTextV1Classes.pageHeaderText]: !children,
                        },
                        className,
                        "page-header-text-v1"
                    )}
                    variant="h4"
                >
                    {children}

                    {/* Loading indicator */}
                    {!children && <Skeleton />}
                </Typography>
            )}
        </>
    );
};
