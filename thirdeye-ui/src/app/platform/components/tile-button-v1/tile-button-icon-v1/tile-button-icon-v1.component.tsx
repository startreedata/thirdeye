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
import { useTileButtonV1 } from "../tile-button-v1";
import { TileButtonIconV1Props } from "./tile-button-icon-v1.interfaces";
import { useTileButtonIconV1Styles } from "./tile-button-icon-v1.styles";

export const TileButtonIconV1: FunctionComponent<TileButtonIconV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const tileButtonIconV1Classes = useTileButtonIconV1Styles();
    const { disabled } = useTileButtonV1();

    return (
        <div
            {...otherProps}
            className={classNames(
                tileButtonIconV1Classes.tileButtonIcon,
                disabled
                    ? tileButtonIconV1Classes.tileButtonIconDisabled
                    : tileButtonIconV1Classes.tileButtonIconEnabled,
                className,
                "tile-button-icon-v1"
            )}
        >
            {children}
        </div>
    );
};
