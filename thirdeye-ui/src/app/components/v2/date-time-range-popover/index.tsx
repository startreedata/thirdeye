/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
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
} from "../../../utils/time-range/time-range.util";
import { timezoneStringShort } from "../../../utils/time/time.util";
import { DateTimeRange } from "../date-time-range";
import { useTimeRangeSelectorStyles } from "./styles";
import { TimeRangeDuration } from "../date-time-range/quick-select";

interface TimeRangeSelectorProps {
    hideRefresh?: boolean;
    showTimeRangeLabel?: boolean;
    hideTimeRangeSelectorButton?: boolean;
    timeRangeDuration: TimeRangeDuration;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    onChange: (timeRangeDuration: TimeRangeDuration) => void;
    onRefresh?: () => void;
    timezone?: string;
}

const TIME_SELECTOR_TEST_IDS = {
    TIME_RANGE_SELECTOR: "time-range-selector",
    OPEN_BUTTON: "open-button",
    APPLY_BUTTON: "apply-button",
    MONTH_LINK: "month-link",
    DATE_LINK: "date-link",
    YEAR_LINK: "year-link",
};

export const DateTimeRangePopover: FunctionComponent<TimeRangeSelectorProps> =
    ({
        hideTimeRangeSelectorButton,
        showTimeRangeLabel,
        hideRefresh,
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        onRefresh,
        onChange,
        timezone,
    }) => {
        const timeRangeSelectorClasses = useTimeRangeSelectorStyles();
        const [
            timeRangeSelectorAnchorElement,
            setTimeRangeSelectorAnchorElement,
        ] = useState<HTMLElement | null>();

        const handleTimeRangeSelectorClick = (
            event: MouseEvent<HTMLElement>
        ): void => {
            setTimeRangeSelectorAnchorElement(event.currentTarget);
        };

        const handleTimeRangeSelectorClose = (): void => {
            setTimeRangeSelectorAnchorElement(null);
        };

        return (
            <Grid
                container
                alignItems="center"
                data-testid={TIME_SELECTOR_TEST_IDS.TIME_RANGE_SELECTOR}
                justifyContent="flex-end"
            >
                {/* Time range */}
                {timeRangeDuration && (
                    <Grid
                        item
                        className={
                            !showTimeRangeLabel
                                ? timeRangeSelectorClasses.timeRangeDisplay
                                : ""
                        }
                    >
                        {/* Time range label */}
                        {showTimeRangeLabel && (
                            <Typography variant="overline">
                                {formatTimeRange(timeRangeDuration.timeRange)}
                            </Typography>
                        )}

                        {/* Time range duration */}
                        <Typography
                            variant="body2"
                            onClick={handleTimeRangeSelectorClick}
                        >
                            {formatTimeRangeDuration(
                                timeRangeDuration,
                                timezone
                            )}{" "}
                            ({timezoneStringShort(timezone)})
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
                            data-testid={TIME_SELECTOR_TEST_IDS.OPEN_BUTTON}
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
                            <DateTimeRange
                                from={{
                                    label: "From",
                                    date: timeRangeDuration?.startTime,
                                }}
                                recentCustomTimeRangeDurations={
                                    recentCustomTimeRangeDurations
                                }
                                selectedRange={timeRangeDuration?.timeRange}
                                to={{
                                    label: "To",
                                    date: timeRangeDuration?.endTime,
                                }}
                                onCancel={handleTimeRangeSelectorClose}
                                onDateApply={onChange}
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
            </Grid>
        );
    };
