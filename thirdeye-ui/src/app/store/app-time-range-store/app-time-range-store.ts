import { isEmpty } from "lodash";
import create, { GetState, SetState } from "zustand";
import { persist } from "zustand/middleware";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import {
    getTimeRangeFromQueryString,
    setTimeRangeInQueryString,
} from "../../utils/params-util/params-util";
import {
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "../../utils/time-range-util/time-range-util";
import { AppTimeRangeStore } from "./app-time-range-store.interfaces";

const LOCAL_STORAGE_KEY_APP_TIME_RANGE = "LOCAL_STORAGE_KEY_APP_TIME_RANGE";
const MAX_ENTRIES_RECENT_TIME_RANGE = 3;

// App store for global time range
export const useAppTimeRangeStore = create<AppTimeRangeStore>(
    persist<AppTimeRangeStore>(
        (
            set: SetState<AppTimeRangeStore>,
            get: GetState<AppTimeRangeStore>
        ) => ({
            // Initialize app time range duration to
            // - time range from query string or
            // - time range from persisted browser local storage or
            // - default
            // in that order of availability
            appTimeRangeDuration: ((): TimeRangeDuration => {
                // From query string
                const timeRangeDurationQueryString = getTimeRangeFromQueryString();
                if (timeRangeDurationQueryString) {
                    return timeRangeDurationQueryString;
                }

                // From persisted browser local storage
                let appTimeRangeStore;
                const appTimeRangeStoreString = localStorage.getItem(
                    LOCAL_STORAGE_KEY_APP_TIME_RANGE
                );
                if (
                    appTimeRangeStoreString &&
                    (appTimeRangeStore = JSON.parse(appTimeRangeStoreString))
                ) {
                    // Also set in query string
                    setTimeRangeInQueryString(
                        appTimeRangeStore.appTimeRangeDuration
                    );

                    return appTimeRangeStore.appTimeRangeDuration;
                }

                // Default
                const timeRangeDurationDefault = getDefaultTimeRangeDuration();

                // Also set in query string
                setTimeRangeInQueryString(timeRangeDurationDefault);

                return timeRangeDurationDefault;
            })(),
            recentCustomTimeRangeDurations: [],

            setAppTimeRangeDuration: (
                timeRangeDuration: TimeRangeDuration
            ): void => {
                if (isEmpty(timeRangeDuration)) {
                    return;
                }

                // Set time range duration
                set({
                    appTimeRangeDuration: timeRangeDuration,
                });

                // Also set in query string
                setTimeRangeInQueryString(timeRangeDuration);

                if (timeRangeDuration.timeRange === TimeRange.CUSTOM) {
                    // Add to recent custom time range durations
                    const { recentCustomTimeRangeDurations } = get();
                    recentCustomTimeRangeDurations.push(timeRangeDuration);

                    // Trim recent custom time range duration entries to set threshold
                    if (
                        recentCustomTimeRangeDurations.length >
                        MAX_ENTRIES_RECENT_TIME_RANGE
                    ) {
                        const newRecentAppTimeRangeDurations = recentCustomTimeRangeDurations.slice(
                            1,
                            MAX_ENTRIES_RECENT_TIME_RANGE + 1
                        );

                        set({
                            recentCustomTimeRangeDurations: newRecentAppTimeRangeDurations,
                        });
                    }
                }
            },

            getAppTimeRangeDuration: (): TimeRangeDuration => {
                const { appTimeRangeDuration } = get();

                if (appTimeRangeDuration.timeRange === TimeRange.CUSTOM) {
                    // Custom time range duration, return as is
                    return appTimeRangeDuration;
                }

                // Predefined time range duration, return current calculated duration
                return getTimeRangeDuration(appTimeRangeDuration.timeRange);
            },
        }),
        {
            name: LOCAL_STORAGE_KEY_APP_TIME_RANGE, // Persist in browser local storage

            // Initial value for app time range duration could be fetched from persisted browser
            // local storage
            // To ensure that persisted value is serialized and deserialized in a familiar manner,
            // serialize and deserialize methods of the Persist middleware are overridden

            serialize: (appTimeRangeStore: AppTimeRangeStore): string => {
                return JSON.stringify(appTimeRangeStore);
            },

            deserialize: (
                appTimeRangeStoreString: string
            ): AppTimeRangeStore => {
                return JSON.parse(appTimeRangeStoreString);
            },
        }
    )
);
