/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageHeaderActionsV1Props } from "./page-header-actions-v1.interfaces";
import { usePageHeaderActionsV1Styles } from "./page-header-actions-v1.styles";

export const PageHeaderActionsV1: FunctionComponent<PageHeaderActionsV1Props> =
    ({ className, children, ...otherProps }) => {
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
