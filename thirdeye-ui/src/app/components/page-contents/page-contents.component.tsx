import {
    Box,
    Grid,
    Hidden,
    Paper,
    Slide,
    Typography,
    withWidth,
} from "@material-ui/core";
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
import { useCommonStyles } from "../../utils/material-ui/common.styles";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { getDocumentTitle } from "../../utils/page/page.util";
import {
    AppBreadcrumbs,
    useAppBreadcrumbs,
} from "../app-breadcrumbs/app-breadcrumbs.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector.component";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.styles";

const THRESHOLD_SCROLL_TOP_PAGE_CONTENTS = 95;

const PageContentsInternal: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();
    const commonClasses = useCommonStyles();
    const [documentTitle, setDocumentTitle] = useState("");
    const [showHeader, setShowHeader] = useState(true);
    const [headerWidth, setHeaderWidth] = useState<number>(
        Dimension.WIDTH_PAGE_CONTENTS_CENTERED
    );
    const [pageContentsScrollTop, setPageContentsScrollTop] = useState(0);
    const { routerBreadcrumbs, pageBreadcrumbs } = useAppBreadcrumbs();
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
        refreshTimeRange,
    } = useTimeRange();
    const pageContentsRef = useRef<HTMLDivElement>(null);
    const mainContentsRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        addWindowResizeListener();

        return removeWindowResizeListener;
    }, []);

    useEffect(() => {
        // Main contents rendered, determine header width
        setHeaderWidthDebounced();
    }, [mainContentsRef]);

    useEffect(() => {
        // Title or breadcrumbs changed, set document title
        setDocumentTitle(generateDocumentTitle());
    }, [props.title, routerBreadcrumbs, pageBreadcrumbs]);

    const addWindowResizeListener = (): void => {
        window.addEventListener("resize", setHeaderWidthDebounced);
    };

    const removeWindowResizeListener = (): void => {
        window.removeEventListener("resize", setHeaderWidthDebounced);
    };

    const setHeaderWidthDebounced = useCallback(
        debounce((): void => {
            if (!mainContentsRef || !mainContentsRef.current) {
                return;
            }

            // Determine header width based on main contents
            setHeaderWidth(mainContentsRef.current.offsetWidth);
        }, 1),
        [mainContentsRef]
    );

    const generateDocumentTitle = (): string => {
        // Document title is composed of:
        // Last router breadcrumb (if available) +
        // title (only if different from last router breadcrumb) +
        // first page breadcrumb (if available) +
        // app name
        const lastRouterBreadcrumbText = !isEmpty(routerBreadcrumbs)
            ? routerBreadcrumbs[routerBreadcrumbs.length - 1].text
            : "";
        const firstPageBreadcrumbText = !isEmpty(pageBreadcrumbs)
            ? pageBreadcrumbs[0].text
            : "";

        return getDocumentTitle(
            lastRouterBreadcrumbText,
            props.title,
            firstPageBreadcrumbText
        );
    };

    const handlePageContentsScroll = (event: UIEvent<HTMLDivElement>): void => {
        if (
            !pageContentsRef ||
            event.target !== pageContentsRef.current ||
            props.hideHeader
        ) {
            return;
        }

        setHeaderVisibilityDebounced(
            (event.target as HTMLDivElement).scrollTop
        );
    };

    const setHeaderVisibilityDebounced = useCallback(
        debounce((newPageContentsScrollTop: number): void => {
            if (newPageContentsScrollTop < pageContentsScrollTop) {
                // Page contents being scrolled up, show header
                setShowHeader(true);
            } else if (
                newPageContentsScrollTop < THRESHOLD_SCROLL_TOP_PAGE_CONTENTS
            ) {
                // Page content scrolled down within set threshold, show header
                setShowHeader(true);
            } else {
                // Page content scrolled down beyond set threshold, hide header
                setShowHeader(false);
            }

            setPageContentsScrollTop(newPageContentsScrollTop);
        }, 1),
        [pageContentsScrollTop]
    );

    return (
        <>
            {/* Document title */}
            <Helmet>
                <title>{documentTitle}</title>
            </Helmet>

            <div
                className={pageContentsClasses.pageContents}
                ref={pageContentsRef}
                onScroll={handlePageContentsScroll}
            >
                <main
                    className={classnames(
                        pageContentsClasses.main,
                        props.centered
                            ? pageContentsClasses.mainCenterAligned
                            : pageContentsClasses.mainExpanded
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
                                        commonClasses.gridLimitation
                                    )}
                                    width={headerWidth}
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
                                            <Grid
                                                item
                                                className={classnames(
                                                    pageContentsClasses.titleContainer,
                                                    commonClasses.ellipsis
                                                )}
                                            >
                                                {/* App breadcrumbs */}
                                                <Hidden
                                                    xsDown
                                                    smDown={
                                                        !props.hideTimeRange
                                                    }
                                                >
                                                    {!props.hideAppBreadcrumbs && (
                                                        <AppBreadcrumbs
                                                            maxRouterBreadcrumbs={
                                                                props.maxRouterBreadcrumbs
                                                            }
                                                        />
                                                    )}
                                                </Hidden>

                                                {/* Title */}
                                                <Typography noWrap variant="h5">
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
                                                        showTimeRange={
                                                            props.width !== "xs"
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

                            {/* Required to clip the container under header */}
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
                            pageContentsClasses.mainContents,
                            commonClasses.gridLimitation
                        )}
                        ref={mainContentsRef}
                    >
                        {props.children}
                    </div>
                </main>
            </div>
        </>
    );
};

export const PageContents = withWidth()(PageContentsInternal);
