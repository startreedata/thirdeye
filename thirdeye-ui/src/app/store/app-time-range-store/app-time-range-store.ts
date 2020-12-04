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

// Application store for global time range
export const useAppTimeRangeStore = create<AppTimeRangeStore>(
    (set: SetState<AppTimeRangeStore>, get: GetState<AppTimeRangeStore>) => ({
        appTimeRange: TimeRange.TODAY,
        startTime: 0,
        endTime: 0,

        setAppTimeRange: (
            timeRange: TimeRange,
            startTime?: number,
            endTime?: number
        ): void => {
            if (timeRange === TimeRange.CUSTOM && startTime && endTime) {
                // Valid custom time range can be set
                set({
                    appTimeRange: timeRange,
                    startTime: startTime,
                    endTime: endTime,
                });

                return;
            }

            // Capture new time range, also default to TimeRange.TODAY at this stage if new time
            // range is TimeRange.CUSTOM
            const newTimeRange =
                timeRange === TimeRange.CUSTOM ? TimeRange.TODAY : timeRange;

            // Set predefined time range
            set({
                appTimeRange: newTimeRange,
                startTime: 0,
                endTime: 0,
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

            // Construct time range duration for a predefined time range
            return getTimeRangeDuration(appTimeRange);
        },
    })
);
