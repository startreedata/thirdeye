import { Grid, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import { getTimeRangeFromQueryString } from "../../utils/params-util/params-util";
import { isTimeRangeDurationEqual } from "../../utils/time-range-util/time-range-util";
import { TimeRangeSelector } from "../time-range-selector/time-range-selector.component";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.style";

export const PageContents: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();
    const [
        appTimeRangeDuration,
        recentCustomTimeRangeDurations,
        setAppTimeRangeDuration,
        getAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRangeDuration,
        state.recentCustomTimeRangeDurations,
        state.setAppTimeRangeDuration,
        state.getAppTimeRangeDuration,
    ]);
    const location = useLocation();

    useEffect(() => {
        // Query string changed, set time range from time range query string if available
        const timeRageDuration = getTimeRangeFromQueryString();
        if (
            !timeRageDuration ||
            isTimeRangeDurationEqual(timeRageDuration, appTimeRangeDuration)
        ) {
            return;
        }

        setAppTimeRangeDuration(timeRageDuration);
    }, [location.search]);

    return (
        <main
            className={classnames(
                pageContentsClasses.outerContainer,
                props.centered
                    ? pageContentsClasses.outerContainerCenterAlign
                    : pageContentsClasses.outerContainerExpand
            )}
        >
            {/* Header, only if title is provided and/or time range is to be displayed */}
            {(props.title || !props.hideTimeRange) && (
                <div
                    className={classnames(
                        pageContentsClasses.container,
                        pageContentsClasses.headerContainer
                    )}
                >
                    <Grid container alignItems="center" justify="space-between">
                        {/* Title */}
                        <Grid item>
                            <Typography variant="h5">{props.title}</Typography>
                        </Grid>

                        {/* Time range selector */}
                        {!props.hideTimeRange && (
                            <Grid item>
                                <TimeRangeSelector
                                    recentCustomTimeRangeDurations={
                                        recentCustomTimeRangeDurations
                                    }
                                    timeRangeDuration={appTimeRangeDuration}
                                    timeRangeDurationFn={
                                        getAppTimeRangeDuration
                                    }
                                    onChange={setAppTimeRangeDuration}
                                />
                            </Grid>
                        )}
                    </Grid>
                </div>
            )}

            {/* Contents */}
            <div
                className={classnames(
                    pageContentsClasses.container,
                    pageContentsClasses.innerContainer
                )}
            >
                {props.children}
            </div>
        </main>
    );
};
