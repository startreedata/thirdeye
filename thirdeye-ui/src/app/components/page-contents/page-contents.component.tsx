import { Box, Grid, Paper, Slide, Typography } from "@material-ui/core";
import classnames from "classnames";
import { debounce } from "lodash";
import React, {
    createRef,
    FunctionComponent,
    UIEvent,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useCommonStyles } from "../../utils/material-ui-util/common-styles.util";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector.component";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.style";

const THRESHOLD_OUTER_CONTAINER_SCROLL_TOP = 110;

export const PageContents: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();
    const commonClasses = useCommonStyles();
    const [outerContainerScrollTop, setOuterContainerScrollTop] = useState(0);
    const [headerContainerWidth, setHeaderContainerWidth] = useState(0);
    const [showHeader, setShowHeader] = useState(true);
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
        refreshTimeRange,
    } = useTimeRange();
    const contentsContainerRef = createRef<HTMLDivElement>();

    useEffect(() => {
        // Determine header container width based on contents container
        if (contentsContainerRef && contentsContainerRef.current) {
            setHeaderContainerWidth(contentsContainerRef.current.offsetWidth);
        }
    }, [contentsContainerRef]);

    const onOuterContainerScroll = (event: UIEvent<HTMLDivElement>): void => {
        setHeaderVisibility((event.target as HTMLDivElement).scrollTop);
    };

    const setHeaderVisibility = useCallback(
        debounce((newOuterContainerScrollTop: number): void => {
            if (newOuterContainerScrollTop < outerContainerScrollTop) {
                // Outer container being scrolled up, show header
                setShowHeader(true);
            } else if (
                newOuterContainerScrollTop <
                THRESHOLD_OUTER_CONTAINER_SCROLL_TOP
            ) {
                // Outer container scrolled down within set threshold, show header
                setShowHeader(true);
            } else {
                // Outer container scrolled down beyond set threshold, hide header
                setShowHeader(false);
            }

            setOuterContainerScrollTop(newOuterContainerScrollTop);
        }, 1),
        [outerContainerScrollTop, setShowHeader, setOuterContainerScrollTop]
    );

    return (
        <div
            className={pageContentsClasses.outerContainer}
            onScroll={onOuterContainerScroll}
        >
            <main
                className={classnames(
                    pageContentsClasses.innerContainer,
                    props.centered
                        ? pageContentsClasses.innerContainerCenterAlign
                        : pageContentsClasses.innerContainerExpand
                )}
            >
                {/* Header, only if title is provided and/or time range is to be displayed */}
                {(props.title || !props.hideTimeRange) && (
                    <>
                        <Slide
                            direction="down"
                            in={showHeader}
                            timeout={{
                                appear: 0,
                                enter: 400,
                                exit: 1000,
                            }}
                        >
                            <Box
                                className={classnames(
                                    pageContentsClasses.headerContainer,
                                    commonClasses.gridLimitation
                                )}
                                width={headerContainerWidth}
                            >
                                <Paper
                                    className={pageContentsClasses.header}
                                    elevation={4}
                                >
                                    <Grid
                                        container
                                        alignItems="center"
                                        className={
                                            pageContentsClasses.headerContents
                                        }
                                        justify="space-between"
                                    >
                                        {/* Title */}
                                        <Grid item>
                                            <Typography variant="h5">
                                                {props.title}
                                            </Typography>
                                        </Grid>

                                        {/* Time range selector */}
                                        {!props.hideTimeRange && (
                                            <Grid item>
                                                <TimeRangeSelector
                                                    recentCustomTimeRangeDurations={
                                                        recentCustomTimeRangeDurations
                                                    }
                                                    timeRangeDuration={
                                                        timeRangeDuration
                                                    }
                                                    onChange={
                                                        setTimeRangeDuration
                                                    }
                                                    onRefresh={refreshTimeRange}
                                                />
                                            </Grid>
                                        )}
                                    </Grid>
                                </Paper>
                            </Box>
                        </Slide>

                        {/* Required to clip the subsequent container under header */}
                        <div
                            className={pageContentsClasses.headerPlaceholder}
                        />
                    </>
                )}

                {/* Contents */}
                <div
                    className={classnames(
                        pageContentsClasses.contentsContainer,
                        commonClasses.gridLimitation
                    )}
                    ref={contentsContainerRef}
                >
                    {props.children}
                </div>
            </main>
        </div>
    );
};
