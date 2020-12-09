import create, { GetState, SetState } from "zustand";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import {
    createTimeRangeDuration,
    getTimeRangeDuration,
} from "../../utils/time-range-util/time-range-util";
import { AppTimeRangeStore } from "./app-time-range-store.interfaces";

const MAX_RECENT_TIME_RANGE_ENTRIES = 3;

// App store for global time range
export const useAppTimeRangeStore = create<AppTimeRangeStore>(
    (set: SetState<AppTimeRangeStore>, get: GetState<AppTimeRangeStore>) => ({
        appTimeRange: TimeRange.TODAY,
        startTime: 0,
        endTime: 0,
        recentCustomTimeRangeDurations: [],

        setAppTimeRange: (
            timeRange: TimeRange,
            startTime?: number,
            endTime?: number
        ): void => {
            const { recentCustomTimeRangeDurations } = get();

            let newTimeRange = TimeRange.TODAY;
            let newStartTime = 0;
            let newEndTime = 0;

            // Capture new time range
            newTimeRange = timeRange;

            if (timeRange === TimeRange.CUSTOM && startTime && endTime) {
                // Valid custom time range can be set
                newStartTime = startTime;
                newEndTime = endTime;

                // Add to recent custom time range durations
                recentCustomTimeRangeDurations.push(
                    createTimeRangeDuration(
                        newTimeRange,
                        newStartTime,
                        newEndTime
                    )
                );

                if (
                    recentCustomTimeRangeDurations.length >
                    MAX_RECENT_TIME_RANGE_ENTRIES
                ) {
                    // Trim recent custom time range duration entries to set threshold
                    const newRecentAppTimeRangeDurations = recentCustomTimeRangeDurations.slice(
                        1,
                        MAX_RECENT_TIME_RANGE_ENTRIES + 1
                    );

                    set({
                        recentCustomTimeRangeDurations: newRecentAppTimeRangeDurations,
                    });
                }
            }

            // Set time range
            set({
                appTimeRange: newTimeRange,
                startTime: newStartTime,
                endTime: newEndTime,
            });
        },

        getAppTimeRangeDuration: (): TimeRangeDuration => {
            const { appTimeRange, startTime, endTime } = get();

            if (appTimeRange === TimeRange.CUSTOM) {
                // Construct time range duration using state values
                return createTimeRangeDuration(
                    appTimeRange,
                    startTime,
                    endTime
                );
            }

            return getTimeRangeDuration(appTimeRange);
        },
    })
);
