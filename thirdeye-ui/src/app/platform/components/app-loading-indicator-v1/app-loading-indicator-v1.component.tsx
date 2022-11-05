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
import { CircularProgress } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { AppLoadingIndicatorV1Props } from "./app-loading-indicator-v1.interfaces";
import { useAppLoadingIndicatorV1Styles } from "./app-loading-indicator-v1.styles";

export const AppLoadingIndicatorV1: FunctionComponent<AppLoadingIndicatorV1Props> =
    ({ className, ...otherProps }) => {
        const appLoadingIndicatorV1Classes = useAppLoadingIndicatorV1Styles();

        return (
            <div
                {...otherProps}
                className={classNames(
                    appLoadingIndicatorV1Classes.appLoadingIndicator,
                    className,
                    "app-loading-indicator-v1"
                )}
            >
                <CircularProgress color="primary" />
            </div>
        );
    };
