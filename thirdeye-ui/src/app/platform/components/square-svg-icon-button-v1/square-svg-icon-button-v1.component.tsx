// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Button } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import { SquareSvgIconButtonV1Props } from "./square-svg-icon-button-v1.interfaces";
import { useSquareSvgIconButtonV1Styles } from "./square-svg-icon-button-v1.styles";

export const SquareSvgIconButtonV1: FunctionComponent<
    SquareSvgIconButtonV1Props
> = ({
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
