import { Grid, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { useTimeRangeStore } from "../../store/time-range-store/time-range-store";
import { TimeRangeSelector } from "../time-range/time-range-selector.component";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.style";

export const PageContents: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();
    const [timeRange, setTimeRange] = useTimeRangeStore((state) => [
        state.timeRange,
        state.setTimeRange,
    ]);

    return (
        <main
            className={classnames(
                pageContentsClasses.container,
                props.centerAlign
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
                                {/* <DateRangePicker /> */}
                                <TimeRangeSelector
                                    timeRange={timeRange}
                                    onChange={setTimeRange}
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
