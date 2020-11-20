export interface DateRange {
    from: Date;
    to: Date;
}

export type DateRangePicker = {
    dateRange: DateRange;
    setDateRange: (dateRange: DateRange) => void;
    getDateRange: () => DateRange;
};
