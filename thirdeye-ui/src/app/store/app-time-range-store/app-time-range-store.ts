import { cloneDeep } from "lodash";
import create, { GetState, SetState } from "zustand";
import { persist } from "zustand/middleware";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import {
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "../../utils/time-range-util/time-range-util";
import { AppTimeRangeStore } from "./app-time-range-store.interfaces";

const LOCAL_STORAGE_KEY_APP_TIME_RANGE = "LOCAL_STORAGE_KEY_APP_TIME_RANGE";
const MAX_ENTRIES_RECENT_CUSTOM_TIME_RANGE_DURATIONS = 3;

// App store for app time range, persisted in browser local storage
export const useAppTimeRangeStore = create<AppTimeRangeStore>(
    persist<AppTimeRangeStore>(
        (
            set: SetState<AppTimeRangeStore>,
            get: GetState<AppTimeRangeStore>
        ) => ({
            appTimeRangeDuration: getDefaultTimeRangeDuration(),
            recentCustomTimeRangeDurations: [],

            setAppTimeRangeDuration: (
                timeRangeDuration: TimeRangeDuration
            ): void => {
                if (!timeRangeDuration) {
                    return;
                }

                set({
                    appTimeRangeDuration: timeRangeDuration,
                });

                if (timeRangeDuration.timeRange === TimeRange.CUSTOM) {
                    // Add to recent custom time range durations
                    const { recentCustomTimeRangeDurations } = get();
                    const newRecentCustomTimeRangeDurations = [
                        ...recentCustomTimeRangeDurations,
                        timeRangeDuration,
                    ];

                    // Trim recent custom time range duration entries to set threshold
                    if (
                        newRecentCustomTimeRangeDurations.length >
                        MAX_ENTRIES_RECENT_CUSTOM_TIME_RANGE_DURATIONS
                    ) {
                        newRecentCustomTimeRangeDurations.splice(
                            0,
                            newRecentCustomTimeRangeDurations.length -
                                MAX_ENTRIES_RECENT_CUSTOM_TIME_RANGE_DURATIONS
                        );
                    }

                    set({
                        recentCustomTimeRangeDurations: newRecentCustomTimeRangeDurations,
                    });
                }
            },

            refreshAppTimeRangeDuration: (): void => {
                const { appTimeRangeDuration } = get();

                if (appTimeRangeDuration.timeRange === TimeRange.CUSTOM) {
                    // Custom time range, set as is
                    set({
                        appTimeRangeDuration: cloneDeep(appTimeRangeDuration),
                    });

                    return;
                }

                // Predefined time range, set current calculated duration
                set({
                    appTimeRangeDuration: getTimeRangeDuration(
                        appTimeRangeDuration.timeRange
                    ),
                });
            },
        }),
        {
            name: LOCAL_STORAGE_KEY_APP_TIME_RANGE, // Persist in browser local storage
        }
    )
);
