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
import { Card, CardContent } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageContentsCardV1Props } from "./page-contents-card-v1.interfaces";
import { usePageContentsCardV1Styles } from "./page-contents-card-v1.styles";

export const PageContentsCardV1: FunctionComponent<PageContentsCardV1Props> = ({
    fullHeight,
    disablePadding,
    className,
    children,
    ...otherProps
}) => {
    const pageContentsCardV1Classes = usePageContentsCardV1Styles();

    return (
        <Card
            {...otherProps}
            className={classNames(
                pageContentsCardV1Classes.pageContentsCard,
                {
                    [pageContentsCardV1Classes.pageContentsCardFullHeight]:
                        fullHeight,
                    [pageContentsCardV1Classes.pageContentsCardPaddingDisabled]:
                        disablePadding,
                },
                className,
                "page-contents-card-v1"
            )}
        >
            <CardContent
                className={classNames(
                    {
                        [pageContentsCardV1Classes.pageContentsCardFullHeight]:
                            fullHeight,
                        [pageContentsCardV1Classes.pageContentsCardPaddingDisabled]:
                            disablePadding,
                    },
                    "page-contents-card-v1-contents"
                )}
            >
                {children}
            </CardContent>
        </Card>
    );
};
