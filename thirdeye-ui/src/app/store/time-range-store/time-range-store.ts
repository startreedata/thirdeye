import create, { SetState } from "zustand";
import {
    TimeRange,
    TimeRangeType,
} from "../../components/time-range/time-range-selector.interfaces";
import { getTimeRange } from "../../utils/time-range-util/time-range-util";
import { TimeRangeStore } from "./time-range-store.interfaces";

// Application store for time range
export const useTimeRangeStore = create<TimeRangeStore>(
    (set: SetState<TimeRangeStore>) => ({
        timeRange: getTimeRange(TimeRangeType.TODAY),

        setTimeRange: (timeRange: TimeRange): void => {
            set({
                timeRange: timeRange
                    ? timeRange
                    : getTimeRange(TimeRangeType.TODAY),
            });
        },
    })
);
