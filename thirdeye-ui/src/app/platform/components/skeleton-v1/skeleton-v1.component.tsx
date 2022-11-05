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
