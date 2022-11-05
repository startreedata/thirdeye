/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Button, Grid, Popover, Typography } from "@material-ui/core";
import CalendarTodayIcon from "@material-ui/icons/CalendarToday";
import RefreshIcon from "@material-ui/icons/Refresh";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import {
    formatTimeRange,
    formatTimeRangeDuration,
} from "../../../../utils/time-range/time-range.util";
import { SafariMuiGridFix } from "../../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { TimeRangeSelectorPopoverContent } from "../../time-range-selector-popover-content/time-range-selector-popover-content.component";
import { TimeRangeSelectorProps } from "./time-range-selector.interfaces";
import { useTimeRangeSelectorStyles } from "./time-range-selector.styles";

export const TimeRangeSelector: FunctionComponent<TimeRangeSelectorProps> = ({
    hideTimeRangeSelectorButton,
    hideTimeRange,
    hideRefresh,
    timeRangeDuration,
    recentCustomTimeRangeDurations,
    onRefresh,
    onChange,
}) => {
    const timeRangeSelectorClasses = useTimeRangeSelectorStyles();
    const [timeRangeSelectorAnchorElement, setTimeRangeSelectorAnchorElement] =
        useState<HTMLElement | null>();

    const handleTimeRangeSelectorClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setTimeRangeSelectorAnchorElement(event.currentTarget);
    };

    const handleTimeRangeSelectorClose = (): void => {
        setTimeRangeSelectorAnchorElement(null);
    };

    return (
        <Grid container alignItems="center" justifyContent="flex-end">
            {/* Time range */}
            {!hideTimeRange && timeRangeDuration && (
                <Grid item>
                    {/* Time range label */}
                    <Typography variant="overline">
                        {formatTimeRange(timeRangeDuration.timeRange)}
                    </Typography>

                    {/* Time range duration */}
                    <Typography variant="body2">
                        {formatTimeRangeDuration(timeRangeDuration)}
                    </Typography>
                </Grid>
            )}

            {!hideTimeRangeSelectorButton && (
                <Grid item>
                    {/* Time range selector button */}
                    <Button
                        className={
                            timeRangeSelectorClasses.timeRangeSelectorButton
                        }
                        color="primary"
                        variant="outlined"
                        onClick={handleTimeRangeSelectorClick}
                    >
                        <CalendarTodayIcon />
                    </Button>

                    {/* Time range selector */}
                    <Popover
                        anchorEl={timeRangeSelectorAnchorElement}
                        open={Boolean(timeRangeSelectorAnchorElement)}
                        onClose={handleTimeRangeSelectorClose}
                    >
                        <TimeRangeSelectorPopoverContent
                            recentCustomTimeRangeDurations={
                                recentCustomTimeRangeDurations
                            }
                            timeRangeDuration={timeRangeDuration}
                            onChange={onChange}
                            onClose={handleTimeRangeSelectorClose}
                        />
                    </Popover>
                </Grid>
            )}

            {!hideRefresh && (
                <Grid item>
                    {/* Refresh button */}
                    <Button
                        className={
                            timeRangeSelectorClasses.timeRangeSelectorButton
                        }
                        color="primary"
                        variant="outlined"
                        onClick={onRefresh}
                    >
                        <RefreshIcon />
                    </Button>
                </Grid>
            )}

            {/* Fixes layout in Safari */}
            <SafariMuiGridFix />
        </Grid>
    );
};
