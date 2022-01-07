import { useMediaQuery, useTheme } from "@material-ui/core";
import {
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent } from "react";
import { CreateMenuButton } from "../create-menu-button.component/create-menu-button.component";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
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

    const screenWidthSmUp = useMediaQuery(theme.breakpoints.up("sm"));

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
                        onChange={setTimeRangeDuration}
                        onRefresh={refreshTimeRange}
                    />
                )}

                {/* Create options button */}
                <CreateMenuButton />
            </PageHeaderActionsV1>
        </PageHeaderV1>
    );
};
