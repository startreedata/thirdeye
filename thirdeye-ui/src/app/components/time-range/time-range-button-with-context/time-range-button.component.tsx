import React, { FunctionComponent } from "react";
import { useSearchParams } from "react-router-dom";
import { TimeRangeButton } from "../time-range-button/time-range-button.component";
import { useTimeRange } from "../time-range-provider/time-range-provider.component";
import {
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../time-range-provider/time-range-provider.interfaces";
import { TimeRangeButtonWithContextProps } from "./time-range-button.interfaces";

export const TimeRangeButtonWithContext: FunctionComponent<
    TimeRangeButtonWithContextProps
> = ({ onTimeRangeChange }) => {
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
    } = useTimeRange();
    const [searchParams, setSearchParams] = useSearchParams();

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

        if (onTimeRangeChange) {
            onTimeRangeChange(
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            );
        }
    };

    return (
        <TimeRangeButton
            recentCustomTimeRangeDurations={recentCustomTimeRangeDurations}
            timeRangeDuration={timeRangeDuration}
            onChange={onHandleTimeRangeChange}
        />
    );
};
