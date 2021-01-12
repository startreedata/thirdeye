import { Grid, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector.component";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.style";

export const PageContents: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
        refreshTimeRange,
    } = useTimeRange();

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
                                    timeRangeDuration={timeRangeDuration}
                                    onChange={setTimeRangeDuration}
                                    onRefresh={refreshTimeRange}
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
