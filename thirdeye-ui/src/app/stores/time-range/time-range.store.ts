import { cloneDeep } from "lodash";
import create from "zustand";
import { persist } from "zustand/middleware";
import { TimeRange } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "../../utils/time-range/time-range.util";
import { TimeRangeStore } from "./time-range.interfaces";

const LOCAL_STORAGE_KEY_TIME_RANGE = "LOCAL_STORAGE_KEY_TIME_RANGE";
const MAX_ITEMS_RECENT_CUSTOM_TIME_RANGE_DURATIONS = 3;

// App store for time range, persisted in browser local storage
export const useTimeRangeStore = create<TimeRangeStore>(
    persist(
        (set, get) => ({
            timeRangeDuration: getDefaultTimeRangeDuration(),
            recentCustomTimeRangeDurations: [],

            setTimeRangeDuration: (timeRangeDuration) => {
                if (!timeRangeDuration) {
                    return;
                }

                set({
                    timeRangeDuration: timeRangeDuration,
                });

                if (timeRangeDuration.timeRange === TimeRange.CUSTOM) {
                    // Add to recent custom time range durations
                    const { recentCustomTimeRangeDurations } = get();
                    const newRecentCustomTimeRangeDurations = [
                        ...recentCustomTimeRangeDurations,
                        timeRangeDuration,
                    ];

                    // Trim recent custom time range duration items to set threshold
                    newRecentCustomTimeRangeDurations.splice(
                        0,
                        newRecentCustomTimeRangeDurations.length -
                            MAX_ITEMS_RECENT_CUSTOM_TIME_RANGE_DURATIONS
                    );

                    set({
                        recentCustomTimeRangeDurations:
                            newRecentCustomTimeRangeDurations,
                    });
                }
            },

            refreshTimeRange: () => {
                const { timeRangeDuration } = get();

                if (timeRangeDuration.timeRange === TimeRange.CUSTOM) {
                    // Custom time range, set as is
                    set({
                        timeRangeDuration: cloneDeep(timeRangeDuration),
                    });

                    return;
                }

                // Predefined time range, set current calculated time range duration
                set({
                    timeRangeDuration: getTimeRangeDuration(
                        timeRangeDuration.timeRange
                    ),
                });
            },
        }),
        {
            name: LOCAL_STORAGE_KEY_TIME_RANGE, // Persist in browser local storage
        }
    )
);
