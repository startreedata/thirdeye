// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { CircularProgress } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { AppLoadingIndicatorV1Props } from "./app-loading-indicator-v1.interfaces";
import { useAppLoadingIndicatorV1Styles } from "./app-loading-indicator-v1.styles";

export const AppLoadingIndicatorV1: FunctionComponent<
    AppLoadingIndicatorV1Props
> = ({ className, ...otherProps }) => {
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
