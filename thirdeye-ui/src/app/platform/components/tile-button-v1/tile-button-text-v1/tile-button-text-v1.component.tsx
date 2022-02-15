// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
