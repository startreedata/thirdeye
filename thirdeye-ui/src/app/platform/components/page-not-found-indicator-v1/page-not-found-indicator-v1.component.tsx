// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Typography } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageNotFoundIndicatorV1Props } from "./page-not-found-indicator-v1.inerfaces";
import { usePageNotFoundIndicatorV1Styles } from "./page-not-found-indicator-v1.styles";

export const PageNotFoundIndicatorV1: FunctionComponent<PageNotFoundIndicatorV1Props> =
    ({ headerText, messageText, className, ...otherProps }) => {
        const pageNotFoundIndicatorV1Classes =
            usePageNotFoundIndicatorV1Styles();

        return (
            <div
                {...otherProps}
                className={classNames(
                    pageNotFoundIndicatorV1Classes.pageNotFoundIndicator,
                    className,
                    "page-not-found-indicator-v1"
                )}
            >
                <div
                    className={
                        pageNotFoundIndicatorV1Classes.pageNotFoundIndicatorContents
                    }
                >
                    <Typography
                        className={classNames(
                            pageNotFoundIndicatorV1Classes.pageNotFoundIndicatorHeader,
                            "page-not-found-indicator-v1-header"
                        )}
                        color="textSecondary"
                        variant="h2"
                    >
                        {headerText}
                    </Typography>

                    <Typography
                        className="page-not-found-indicator-v1-message"
                        color="textSecondary"
                        variant="h4"
                    >
                        {messageText}
                    </Typography>
                </div>
            </div>
        );
    };
