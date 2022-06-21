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
import React, { createContext, FunctionComponent, useContext } from "react";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import {
    TileButtonV1ContextProps,
    TileButtonV1Props,
} from "./tile-button-v1.interfaces";
import { useTileButtonV1Styles } from "./tile-button-v1.styles";

export const TileButtonV1: FunctionComponent<TileButtonV1Props> = ({
    href,
    externalLink,
    target,
    disabled,
    className,
    onClick,
    children,
    ...otherProps
}) => {
    const tileButtonV1Classes = useTileButtonV1Styles();

    const tileButtonV1Context = {
        disabled: Boolean(disabled),
    };

    return (
        <TileButtonV1Context.Provider value={tileButtonV1Context}>
            {/* Using ButtonBase doesn't provide hover and active effects out of the box */}
            {/* Using Button instead helps avoid duplicating those effects */}
            {href && externalLink && (
                // Button as a link
                <Button
                    {...otherProps}
                    className={classNames(
                        tileButtonV1Classes.tileButton,
                        className,
                        "tile-button-v1"
                    )}
                    classes={{
                        label: tileButtonV1Classes.tileButtonLabel,
                    }}
                    disabled={disabled}
                    href={href}
                    target={target}
                >
                    {children}
                </Button>
            )}

            {href && !externalLink && (
                // Button as a router link
                <Button
                    {...otherProps}
                    className={classNames(
                        tileButtonV1Classes.tileButton,
                        className,
                        "tile-button-v1"
                    )}
                    classes={{
                        label: tileButtonV1Classes.tileButtonLabel,
                    }}
                    component={RouterLink}
                    disabled={disabled}
                    target={target}
                    to={href}
                >
                    {children}
                </Button>
            )}

            {!href && (
                // Button with click handler
                <Button
                    {...otherProps}
                    className={classNames(
                        tileButtonV1Classes.tileButton,
                        className,
                        "tile-button-v1"
                    )}
                    classes={{
                        label: tileButtonV1Classes.tileButtonLabel,
                    }}
                    disabled={disabled}
                    onClick={onClick}
                >
                    {children}
                </Button>
            )}
        </TileButtonV1Context.Provider>
    );
};

const TileButtonV1Context = createContext<TileButtonV1ContextProps>(
    {} as TileButtonV1ContextProps
);

export const useTileButtonV1 = (): TileButtonV1ContextProps => {
    return useContext(TileButtonV1Context);
};
