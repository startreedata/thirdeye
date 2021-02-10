import { Box, Grid, Paper, Slide, Typography } from "@material-ui/core";
import classnames from "classnames";
import { debounce, isEmpty } from "lodash";
import React, {
    FunctionComponent,
    UIEvent,
    useCallback,
    useEffect,
    useRef,
    useState,
} from "react";
import { Helmet } from "react-helmet";
import { useWindowSize } from "../../utils/hooks/useWindowSize";
import { useCommonStyles } from "../../utils/material-ui/common.styles";
import { getDocumentTitle } from "../../utils/page/page.util";
import {
    AppBreadcrumbs,
    useAppBreadcrumbs,
} from "../app-breadcrumbs/app-breadcrumbs.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector.component";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.style";

const THRESHOLD_OUTER_CONTAINER_SCROLL_TOP = 95;

export const PageContents: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();
    const commonClasses = useCommonStyles();
    const [documentTitle, setDocumentTitle] = useState("");
    const [outerContainerScrollTop, setOuterContainerScrollTop] = useState(0);
    const [headerContainerWidth, setHeaderContainerWidth] = useState(0);
    const [headerContainerFullWidth, setHeaderContainerFullWidth] = useState(
        false
    );
    const [showHeader, setShowHeader] = useState(true);
    const { routerBreadcrumbs, pageBreadcrumbs } = useAppBreadcrumbs();
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
        refreshTimeRange,
    } = useTimeRange();
    const outerContainerRef = useRef<HTMLDivElement>(null);
    const contentsContainerRef = useRef<HTMLDivElement>(null);

    const { windowWidth } = useWindowSize();

    useEffect(() => {
        // Title or breadcrumbs changed, set document title
        setDocumentTitle(generateDocumentTitle());
    }, [props.title, routerBreadcrumbs, pageBreadcrumbs]);

    useEffect(() => {
        // Contents container rendered or window width changed, determine header width
        setHeaderWidthDebounced();
    }, [contentsContainerRef, windowWidth]);

    const onOuterContainerScroll = (event: UIEvent<HTMLDivElement>): void => {
        if (
            !outerContainerRef ||
            event.target !== outerContainerRef.current ||
            props.hideHeader
        ) {
            return;
        }

        setHeaderVisibilityDebounced(
            (event.target as HTMLDivElement).scrollTop
        );
    };

    const generateDocumentTitle = (): string => {
        // Document title is composed of
        // Last router breadcrumb (if available) +
        // title (only if different from last router breadcrumb) +
        // first page breadcrumb (if available) +
        // app name
        const routerBreadcrumbText =
            (!isEmpty(routerBreadcrumbs) &&
                routerBreadcrumbs[routerBreadcrumbs.length - 1].text) ||
            "";
        const pageBreadcrumbText =
            (!isEmpty(pageBreadcrumbs) && pageBreadcrumbs[0].text) || "";

        return getDocumentTitle(
            routerBreadcrumbText,
            props.title,
            pageBreadcrumbText
        );
    };

    const setHeaderWidthDebounced = useCallback(
        debounce((): void => {
            // Determine header container width based on contents container and window width
            if (contentsContainerRef && contentsContainerRef.current) {
                const contentsContainerOffsetWidth =
                    contentsContainerRef.current.offsetWidth;
                setHeaderContainerWidth(contentsContainerOffsetWidth);
                // If contents container overflows window width (window has a horizontal scroll),
                // header container to occupy full window width instead
                setHeaderContainerFullWidth(
                    contentsContainerOffsetWidth > window.innerWidth
                );
            }
        }, 1),
        [contentsContainerRef]
    );

    const setHeaderVisibilityDebounced = useCallback(
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
        [outerContainerScrollTop]
    );

    return (
        <>
            {/* Document title */}
            <Helmet>
                <title>{documentTitle}</title>
            </Helmet>

            <div
                className={pageContentsClasses.outerContainer}
                ref={outerContainerRef}
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
                    {/* Header */}
                    {!props.hideHeader && (
                        <>
                            <Slide
                                direction="down"
                                in={showHeader}
                                timeout={{
                                    enter: 400,
                                    exit: 1000,
                                }}
                            >
                                <Box
                                    className={classnames(
                                        pageContentsClasses.headerContainer,
                                        commonClasses.gridLimitation,
                                        headerContainerFullWidth
                                            ? pageContentsClasses.headerContainerFullWidth
                                            : ""
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
                                            <Grid item>
                                                {/* App breadcrumbs */}
                                                {!props.hideAppBreadcrumbs && (
                                                    <AppBreadcrumbs
                                                        maxRouterBreadcrumbs={
                                                            props.maxRouterBreadcrumbs
                                                        }
                                                    />
                                                )}

                                                {/* Title */}
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
                                                        onRefresh={
                                                            refreshTimeRange
                                                        }
                                                    />
                                                </Grid>
                                            )}
                                        </Grid>
                                    </Paper>
                                </Box>
                            </Slide>

                            {/* Required to clip the subsequent container under header */}
                            <div
                                className={
                                    pageContentsClasses.headerPlaceholder
                                }
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
        </>
    );
};
