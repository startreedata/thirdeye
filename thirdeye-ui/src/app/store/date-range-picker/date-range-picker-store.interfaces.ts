export interface DateRange {
    from: Date;
    to: Date;
    predefineRangeName: string;
}

export type DateRangePicker = {
    dateRange: DateRange;
    setDateRange: (dateRange: DateRange) => void;
    getDateRange: () => DateRange;
};
