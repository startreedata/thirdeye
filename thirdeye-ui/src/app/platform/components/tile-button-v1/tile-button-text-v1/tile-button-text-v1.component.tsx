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
import { Typography } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { TileButtonTextV1Props } from "./tile-button-text-v1.interfaces";
import { useTileButtonTextV1Styles } from "./tile-button-text-v1.styles";

export const TileButtonTextV1: FunctionComponent<TileButtonTextV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const tileButtonTextV1Classes = useTileButtonTextV1Styles();

    return (
        <Typography
            {...otherProps}
            noWrap
            className={classNames(
                tileButtonTextV1Classes.tileButtonText,
                className,
                "tile-button-text-v1"
            )}
            variant="body1"
        >
            {children}
        </Typography>
    );
};
