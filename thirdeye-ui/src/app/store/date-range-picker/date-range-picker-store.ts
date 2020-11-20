import create, { SetState } from "zustand";
import {
    DateRange,
    DateRangePicker,
} from "./date-range-picker-store.interfaces";

export const useDateRangePickerStore = create<DateRangePicker>(
    (set: SetState<DateRangePicker>, get) => ({
        dateRange: {} as DateRange,

        setDateRange: (dateRange: DateRange): void => {
            set({ dateRange });
        },

        getDateRange: (): DateRange => get().dateRange,
    })
);
