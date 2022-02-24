import { Grid, useMediaQuery, useTheme } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import {
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "../../platform/components";
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
            <Grid container justifyContent="space-between">
                <Grid item>
                    <PageHeaderTextV1>{props.title}</PageHeaderTextV1>
                </Grid>
                <Grid item>
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
                        {props.showCreateButton && <CreateMenuButton />}
                    </PageHeaderActionsV1>
                </Grid>
            </Grid>
        </PageHeaderV1>
    );
};
