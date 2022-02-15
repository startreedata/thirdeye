// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Box, Button, CircularProgress } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent, useEffect, useRef, useState } from "react";
import { LoadingButtonV1Props } from "./loading-button-v1.interfaces";

export const LoadingButtonV1: FunctionComponent<LoadingButtonV1Props> = ({
    loading,
    disabled,
    className,
    children,
    ...otherProps
}) => {
    const [loadingInternal, setLoadingInternal] = useState(false); // Start with non-loading state to capture button dimensions
    const [buttonHeight, setButtonHeight] = useState(0);
    const [buttonWidth, setButtonWidth] = useState(0);
    const buttonRef = useRef<HTMLButtonElement>(null);

    useEffect(() => {
        setLoadingInternal(Boolean(loading));
    }, [loading]);

    useEffect(() => {
        if (loadingInternal || !buttonRef || !buttonRef.current) {
            return;
        }

        // Capture button dimensions to be used when loading
        setButtonHeight(buttonRef.current.getBoundingClientRect().height);
        setButtonWidth(buttonRef.current.getBoundingClientRect().width);
    }, [loadingInternal, buttonRef]);

    return (
        <Box
            height={loadingInternal ? buttonHeight : "auto"}
            width={loadingInternal ? buttonWidth : "auto"}
        >
            <Button
                {...otherProps}
                className={classNames(className, "loading-button-v1")}
                disabled={disabled || loadingInternal}
                ref={buttonRef}
                style={
                    loadingInternal
                        ? { height: buttonHeight, width: buttonWidth }
                        : {}
                }
            >
                {!loadingInternal && children}

                {/* Loading indicator */}
                {loadingInternal && (
                    <CircularProgress color="inherit" size={20} />
                )}
            </Button>
        </Box>
    );
};
