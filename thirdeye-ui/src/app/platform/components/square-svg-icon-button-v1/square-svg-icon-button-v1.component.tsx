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
import { Button } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import { SquareSvgIconButtonV1Props } from "./square-svg-icon-button-v1.interfaces";
import { useSquareSvgIconButtonV1Styles } from "./square-svg-icon-button-v1.styles";

export const SquareSvgIconButtonV1: FunctionComponent<SquareSvgIconButtonV1Props> =
    ({
        href,
        externalLink,
        target,
        color,
        disabled,
        svgIcon: SvgIcon,
        className,
        onClick,
        ...otherProps
    }) => {
        const squareSvgIconButtonV1Classes = useSquareSvgIconButtonV1Styles();

        return (
            <>
                {href && externalLink && (
                    // Button as a link
                    <Button
                        {...otherProps}
                        className={classNames(
                            squareSvgIconButtonV1Classes.squareSvgIconButton,
                            className,
                            "square-svg-icon-button-v1"
                        )}
                        color={color}
                        disabled={disabled}
                        href={href}
                        target={target}
                    >
                        <SvgIcon />
                    </Button>
                )}

                {href && !externalLink && (
                    // Button as a router link
                    <Button
                        {...otherProps}
                        className={classNames(
                            squareSvgIconButtonV1Classes.squareSvgIconButton,
                            className,
                            "square-svg-icon-button-v1"
                        )}
                        color={color}
                        component={RouterLink}
                        disabled={disabled}
                        target={target}
                        to={href}
                    >
                        <SvgIcon />
                    </Button>
                )}

                {!href && (
                    // Button with click handler
                    <Button
                        {...otherProps}
                        className={classNames(
                            squareSvgIconButtonV1Classes.squareSvgIconButton,
                            className,
                            "square-svg-icon-button-v1"
                        )}
                        color={color}
                        disabled={disabled}
                        onClick={onClick}
                    >
                        <SvgIcon />
                    </Button>
                )}
            </>
        );
    };
