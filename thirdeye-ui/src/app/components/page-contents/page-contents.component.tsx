import { Grid, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, useEffect } from "react";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import { getTimeRangeFromQueryString } from "../../utils/params-util/params-util";
import { TimeRangeSelector } from "../time-range-selector/time-range-selector.component";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.style";

export const PageContents: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();
    const [
        appTimeRange,
        recentCustomTimeRangeDurations,
        setAppTimeRange,
        getAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRange,
        state.recentCustomTimeRangeDurations,
        state.setAppTimeRange,
        state.getAppTimeRangeDuration,
    ]);

    useEffect(() => {
        // Set time range from time range query string if available
        const timeRageDuration = getTimeRangeFromQueryString();
        if (!timeRageDuration) {
            return;
        }

        setAppTimeRange(
            timeRageDuration.timeRange,
            timeRageDuration.startTime,
            timeRageDuration.endTime
        );
    }, []);

    return (
        <main
            className={classnames(
                pageContentsClasses.container,
                props.contentsCenterAlign
                    ? pageContentsClasses.centeredContainer
                    : pageContentsClasses.expandedContainer
            )}
        >
            <Grid container direction="column">
                {/* Header, only if title is provided and/or time range is to be displayed */}
                {(props.title || !props.hideTimeRange) && (
                    <Grid
                        container
                        item
                        alignItems="center"
                        className={pageContentsClasses.header}
                        justify="space-between"
                    >
                        {/* Title */}
                        <Grid
                            item
                            className={
                                props.titleCenterAlign
                                    ? pageContentsClasses.titleCenterAlign
                                    : ""
                            }
                        >
                            <Typography variant="h5">{props.title}</Typography>
                        </Grid>

                        {/* Time range selector */}
                        {!props.hideTimeRange && (
                            <Grid item>
                                <TimeRangeSelector
                                    getTimeRangeDuration={
                                        getAppTimeRangeDuration
                                    }
                                    recentCustomTimeRangeDurations={
                                        recentCustomTimeRangeDurations
                                    }
                                    timeRange={appTimeRange}
                                    onChange={setAppTimeRange}
                                />
                            </Grid>
                        )}
                    </Grid>
                )}

                {/* Contents */}
                <Grid item>
                    {/* Include children */}
                    {props.children}
                </Grid>
            </Grid>
        </main>
    );
};
