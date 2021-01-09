import { Grid, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
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
        refreshAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRangeDuration,
        state.recentCustomTimeRangeDurations,
        state.setAppTimeRangeDuration,
        state.refreshAppTimeRangeDuration,
    ]);

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
                                    onChange={setAppTimeRangeDuration}
                                    onRefresh={refreshAppTimeRangeDuration}
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
