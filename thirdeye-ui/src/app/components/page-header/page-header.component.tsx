import { useMediaQuery, useTheme } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useSearchParams } from "react-router-dom";
import {
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "../../platform/components";
import { CreateMenuButton } from "../create-menu-button.component/create-menu-button.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import {
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector/time-range-selector.component";
import { PageHeaderProps } from "./page-header.interfaces";

export const PageHeader: FunctionComponent<PageHeaderProps> = (
    props: PageHeaderProps
) => {
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
        refreshTimeRange,
    } = useTimeRange();
    const theme = useTheme();
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

    return (
        <PageHeaderV1>
            <PageHeaderTextV1>{props.title}</PageHeaderTextV1>

            <PageHeaderActionsV1>
                {/* Time range selector */}
                {props.showTimeRange && (
                    <TimeRangeSelector
                        hideTimeRange={!screenWidthSmUp}
                        recentCustomTimeRangeDurations={
                            recentCustomTimeRangeDurations
                        }
                        timeRangeDuration={timeRangeDuration}
                        onChange={onHandleTimeRangeChange}
                        onRefresh={refreshTimeRange}
                    />
                )}

                {/* Create options button */}
                {props.showCreateButton && <CreateMenuButton />}
            </PageHeaderActionsV1>
        </PageHeaderV1>
    );
};
