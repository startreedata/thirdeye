import create, { GetState, SetState } from "zustand";
import { persist } from "zustand/middleware";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import { setTimeRangeInQueryString } from "../../utils/params-util/params-util";
import {
    createTimeRangeDuration,
    getTimeRangeDuration,
} from "../../utils/time-range-util/time-range-util";
import { AppTimeRangeStore } from "./app-time-range-store.interfaces";

const LOCAL_STORAGE_KEY_APP_TIME_RANGE = "LOCAL_STORAGE_KEY_APP_TIME_RANGE";
const MAX_RECENT_TIME_RANGE_ENTRIES = 3;

// App store for global time range
export const useAppTimeRangeStore = create<AppTimeRangeStore>(
    persist<AppTimeRangeStore>(
        (
            set: SetState<AppTimeRangeStore>,
            get: GetState<AppTimeRangeStore>
        ) => ({
            appTimeRange: TimeRange.TODAY,
            startTime: 0,
            endTime: 0,
            recentCustomTimeRangeDurations: [],

            setAppTimeRange: (
                timeRange: TimeRange,
                startTime = 0,
                endTime = 0
            ): void => {
                // Set time range
                set({
                    appTimeRange: timeRange,
                    startTime: startTime,
                    endTime: endTime,
                });

                if (timeRange === TimeRange.CUSTOM) {
                    // Add to recent custom time range durations
                    const { recentCustomTimeRangeDurations } = get();
                    recentCustomTimeRangeDurations.push(
                        createTimeRangeDuration(timeRange, startTime, endTime)
                    );

                    // Trim recent custom time range duration entries to set threshold
                    if (
                        recentCustomTimeRangeDurations.length >
                        MAX_RECENT_TIME_RANGE_ENTRIES
                    ) {
                        const newRecentAppTimeRangeDurations = recentCustomTimeRangeDurations.slice(
                            1,
                            MAX_RECENT_TIME_RANGE_ENTRIES + 1
                        );

                        set({
                            recentCustomTimeRangeDurations: newRecentAppTimeRangeDurations,
                        });
                    }
                }
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
        }),
        {
            name: LOCAL_STORAGE_KEY_APP_TIME_RANGE, // Persist in browser local storage

            serialize: (appTimeRangeStore: AppTimeRangeStore): string => {
                // While serializing the store to persist in browser local storage, set time range
                // in query string
                setTimeRangeInQueryString(
                    appTimeRangeStore.appTimeRange,
                    appTimeRangeStore.startTime,
                    appTimeRangeStore.endTime
                );

                return JSON.stringify(appTimeRangeStore);
            },

            deserialize: (
                appTimeRangeStoreString: string
            ): AppTimeRangeStore => {
                const appTimeRangeStore = JSON.parse(appTimeRangeStoreString);

                // While deserializing the store from browser local storage, set time range in
                // query string
                setTimeRangeInQueryString(
                    appTimeRangeStore.appTimeRange,
                    appTimeRangeStore.startTime,
                    appTimeRangeStore.endTime
                );

                return appTimeRangeStore;
            },
        }
    )
);
