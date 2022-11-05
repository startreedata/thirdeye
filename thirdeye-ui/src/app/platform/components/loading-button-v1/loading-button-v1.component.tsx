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
