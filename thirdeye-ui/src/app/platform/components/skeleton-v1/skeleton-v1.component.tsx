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
import { Skeleton } from "@material-ui/lab";
import classNames from "classnames";
import React, { FunctionComponent, useEffect, useState } from "react";
import { SkeletonV1Props } from "./skeleton-v1.interfaces";

export const DEFAULT_DELAY = 100;

export const SkeletonV1: FunctionComponent<SkeletonV1Props> = ({
    className,
    delayInMS = DEFAULT_DELAY,
    preventDelay,
    ...props
}) => {
    const [showLoading, setShowLoading] = useState(false);

    useEffect(() => {
        let delayTimeout: number;

        if (!preventDelay) {
            // if the loading indicator should appear on on a timeout, render it
            // after the timeout finishes
            delayTimeout = window.setTimeout(() => {
                // don't set state if component has been unmounted
                setShowLoading(true);
            }, delayInMS);
        } else {
            setShowLoading(true);
        }

        // on unmount
        return () => {
            delayTimeout && clearTimeout(delayTimeout);
        };
    }, []);

    if (!showLoading) {
        return null;
    }

    return (
        <Skeleton className={classNames("skeleton-v1", className)} {...props} />
    );
};
