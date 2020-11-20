import moment from "moment";

export const parseDate = (date: Date, format = "MMM DD, YY"): string => {
    return moment(date).format(format);
};

export const parseTime = (date: Date, format = "h:mm a"): string => {
    return moment(date).format(format);
};

export const parseDateTime = (
    date: Date,
    format = "MMM DD, YY h:mm a"
): string => {
    return moment(date).format(format);
};

export const getRelativeTime = (date: Date, from: Date): string => {
    return from ? moment(date).from(from) : moment(date).fromNow();
};

export const addDate = (
    baseDate: Date,
    durationUnit: moment.DurationInputArg1,
    duration: moment.DurationInputArg2
): Date => {
    return moment(baseDate).add(durationUnit, duration).toDate();
};

export const subtractDate = (
    baseDate: Date,
    durationUnit: moment.DurationInputArg1,
    duration: moment.DurationInputArg2
): Date => {
    return moment(baseDate).subtract(durationUnit, duration).toDate();
};
