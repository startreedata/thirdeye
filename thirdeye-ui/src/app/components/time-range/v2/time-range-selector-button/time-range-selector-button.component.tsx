// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Button, Popover } from "@material-ui/core";
import CalendarTodayIcon from "@material-ui/icons/CalendarToday";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { formatStartAndEndDuration } from "../../../../utils/time-range/time-range.util";
import { TimeRangeSelectorPopoverContent } from "../time-range-selector-popover-content/time-range-selector-popover-content.component";
import { TimeRangeSelectorButtonProps } from "./time-range-selector-button.interfaces";

export const TimeRangeSelectorButton: FunctionComponent<TimeRangeSelectorButtonProps> =
    ({
        start,
        end,
        recentCustomTimeRangeDurations,
        onChange,
        maxDate,
        minDate,
        btnGroupColor = "secondary",
        fullWidth,
        placeholder,
    }) => {
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
            <>
                <Button
                    color={btnGroupColor}
                    fullWidth={fullWidth}
                    startIcon={<CalendarTodayIcon />}
                    variant="outlined"
                    onClick={handleTimeRangeSelectorClick}
                >
                    {start && end && formatStartAndEndDuration(start, end)}
                    {!start && !end && placeholder}
                </Button>
                {/* Time range selector */}
                <Popover
                    anchorEl={timeRangeSelectorAnchorElement}
                    open={Boolean(timeRangeSelectorAnchorElement)}
                    onClose={handleTimeRangeSelectorClose}
                >
                    <TimeRangeSelectorPopoverContent
                        end={end}
                        maxDate={maxDate}
                        minDate={minDate}
                        recentCustomTimeRangeDurations={
                            recentCustomTimeRangeDurations
                        }
                        start={start}
                        onChange={onChange}
                        onClose={handleTimeRangeSelectorClose}
                    />
                </Popover>
            </>
        );
    };
