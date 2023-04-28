/*
 * Copyright 2023 StarTree Inc
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
import { Typography, useTheme } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { ReactComponent as EmptyGlassIcon } from "../../../assets/images/empty-glass.svg";
import { NoDataIndicatorProps } from "./no-data-indicator.interfaces";
import { useNoDataIndicatorStyles } from "./no-data-indicator.styles";

const HEIGHT_ICON = 36;

export const NoDataIndicator: FunctionComponent<NoDataIndicatorProps> = ({
    text,
    children,
    className,
}: NoDataIndicatorProps) => {
    const noDataIndicatorClasses = useNoDataIndicatorStyles();
    const theme = useTheme();

    return (
        <div
            className={classNames(
                noDataIndicatorClasses.noDataIndicator,
                className
            )}
        >
            {/* Icon */}
            <div>
                <EmptyGlassIcon
                    fill={theme.palette.primary.main}
                    height={HEIGHT_ICON}
                />
            </div>

            {/* Text */}
            {text && (
                <div>
                    <Typography variant="body2">{text}</Typography>
                </div>
            )}

            {/* children */}
            {children && <div>{children}</div>}
        </div>
    );
};
