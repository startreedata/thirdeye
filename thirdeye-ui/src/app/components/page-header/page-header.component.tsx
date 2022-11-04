import {
    Grid,
    Tab,
    Tabs,
    Typography,
    useMediaQuery,
    useTheme,
} from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { Link as RouterLink, useSearchParams } from "react-router-dom";
import {
    NotificationDisplayV1,
    PageHeaderActionsV1,
} from "../../platform/components";
import { getTimeRangeDuration } from "../../utils/time-range/time-range.util";
import { Breadcrumbs } from "../breadcrumbs/breadcrumbs.component";
import { CreateMenuButton } from "../create-menu-button.component/create-menu-button.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import {
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector/time-range-selector.component";
import { PageHeaderProps } from "./page-header.interfaces";
import { usePageHeaderStyles } from "./page-header.styles";

export const PageHeader: FunctionComponent<PageHeaderProps> = ({
    title,
    subtitle,
    showTimeRange,
    showCreateButton,
    transparentBackground,
    breadcrumbs,
    children,
    customActions,
    subNavigation,
    subNavigationSelected,
}) => {
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
    } = useTimeRange();
    const theme = useTheme();
    const pageHeaderStyles = usePageHeaderStyles();
    const [searchParams, setSearchParams] = useSearchParams();

    const screenWidthSmUp = useMediaQuery(theme.breakpoints.up("sm"));

    const onHandleTimeRangeChange = (
        timeRangeDuration: TimeRangeDuration
    ): void => {
        setTimeRangeDuration(timeRangeDuration);
        searchParams.set(
            TimeRangeQueryStringKey.TIME_RANGE,
            timeRangeDuration.timeRange
        );
        searchParams.set(
            TimeRangeQueryStringKey.START_TIME,
            timeRangeDuration.startTime.toString()
        );
        searchParams.set(
            TimeRangeQueryStringKey.END_TIME,
            timeRangeDuration.endTime.toString()
        );
        setSearchParams(searchParams);
    };

    const onHandleRefresh = (): void => {
        onHandleTimeRangeChange(
            getTimeRangeDuration(timeRangeDuration.timeRange)
        );
    };

    return (
        <>
            <div
                className={classNames(
                    transparentBackground
                        ? pageHeaderStyles.transparent
                        : pageHeaderStyles.pageHeader,
                    subNavigation && subNavigation.length > 0
                        ? pageHeaderStyles.noPaddingBottom
                        : undefined
                )}
            >
                <Grid container>
                    <Grid
                        item
                        className={pageHeaderStyles.noPaddingBottom}
                        xs={12}
                    >
                        {breadcrumbs && <Breadcrumbs crumbs={breadcrumbs} />}
                    </Grid>

                    <Grid item xs={12}>
                        <Grid container justifyContent="space-between">
                            <Grid item>
                                {title && (
                                    <Typography noWrap variant="h4">
                                        {title}
                                    </Typography>
                                )}
                                {children}
                                {subtitle && (
                                    <Typography noWrap variant="subtitle1">
                                        {subtitle}
                                    </Typography>
                                )}
                            </Grid>

                            <Grid item>
                                {(showTimeRange || showCreateButton) && (
                                    <PageHeaderActionsV1>
                                        {/* Time range selector */}
                                        {showTimeRange && (
                                            <TimeRangeSelector
                                                hideTimeRange={!screenWidthSmUp}
                                                recentCustomTimeRangeDurations={
                                                    recentCustomTimeRangeDurations
                                                }
                                                timeRangeDuration={
                                                    timeRangeDuration
                                                }
                                                onChange={
                                                    onHandleTimeRangeChange
                                                }
                                                onRefresh={onHandleRefresh}
                                            />
                                        )}

                                        {/* Create options button */}
                                        {showCreateButton && (
                                            <CreateMenuButton />
                                        )}
                                    </PageHeaderActionsV1>
                                )}

                                {customActions}
                            </Grid>
                        </Grid>
                    </Grid>

                    {subNavigation && subNavigation.length > 0 && (
                        <Grid item xs={12}>
                            <Tabs
                                className="page-header-sub-nav"
                                value={subNavigationSelected}
                            >
                                {subNavigation.map((subNavConfig, idx) => {
                                    return (
                                        <Tab
                                            component={RouterLink}
                                            key={subNavConfig.link}
                                            label={subNavConfig.label}
                                            to={subNavConfig.link}
                                            value={idx}
                                        />
                                    );
                                })}
                            </Tabs>
                        </Grid>
                    )}
                </Grid>
            </div>

            <NotificationDisplayV1 />
        </>
    );
};
