import i18n from "i18next";
import { isNil, round } from "lodash";
import { DateTime, Interval } from "luxon";
import { formatNumberV1 } from "../number/number.util";

// Returns most appropriate formatted string representation of interval between start and end time
// For example:
// 1 second
// 1 day
// 1 month
// Requires the following locale string keys to be available in i18next translation resource
// "label.year-lowercase": "year"
// "label.years-lowercase": "years"
// "label.month-lowercase": "month"
// "label.months-lowercase": "months"
// "label.week-lowercase": "week"
// "label.weeks-lowercase": "weeks"
// "label.day-lowercase": "day"
// "label.days-lowercase": "days"
// "label.hour-lowercase": "hour"
// "label.hours-lowercase": "hours"
// "label.minute-lowercase": "minute"
// "label.minutes-lowercase": "minutes"
// "label.second-lowercase": "second"
// "label.seconds-lowercase": "seconds"
// "label.millisecond-lowercase": "millisecond"
// "label.milliseconds-lowercase": "milliseconds"
export const formatDurationV1 = (
    startTime: number,
    endTime: number
): string => {
    if (isNil(startTime) || isNil(endTime)) {
        return "";
    }

    const duration = Interval.fromDateTimes(
        DateTime.fromMillis(startTime),
        DateTime.fromMillis(endTime)
    ).toDuration();

    let durationInUnits;
    if ((durationInUnits = round(duration.as("years"), 1)) >= 1) {
        // Duration in years
        return `${formatNumberV1(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.year-lowercase")
                : i18n.t("label.years-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("months"), 1)) >= 1) {
        // Duration in months
        return `${formatNumberV1(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.month-lowercase")
                : i18n.t("label.months-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("weeks"), 1)) >= 1) {
        // Duration in weeks
        return `${formatNumberV1(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.week-lowercase")
                : i18n.t("label.weeks-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("days"), 1)) >= 1) {
        // Duration in days
        return `${formatNumberV1(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.day-lowercase")
                : i18n.t("label.days-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("hours"), 1)) >= 1) {
        // Duration in hours
        return `${formatNumberV1(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.hour-lowercase")
                : i18n.t("label.hours-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("minutes"), 1)) >= 1) {
        // Duration in minutes
        return `${formatNumberV1(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.minute-lowercase")
                : i18n.t("label.minutes-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("seconds"), 1)) >= 1) {
        // Duration in seconds
        return `${formatNumberV1(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.second-lowercase")
                : i18n.t("label.seconds-lowercase")
        }`;
    }

    // Duration in milliseconds
    durationInUnits = round(duration.as("milliseconds"), 1);

    return `${formatNumberV1(durationInUnits, 1)} ${
        durationInUnits === 1
            ? i18n.t("label.millisecond-lowercase")
            : i18n.t("label.milliseconds-lowercase")
    }`;
};

// Returns formatted string representation of date and time
// For example:
// MMM DD, YYYY, HH:MM AM/PM
export const formatDateAndTimeV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        month: "short",
        day: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
};

// Returns formatted string representation of date part of date
// For example:
// MMM DD, YYYY
export const formatDateV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        month: "short",
        day: "2-digit",
        year: "numeric",
    });
};

// Returns formatted string representation of time part of date
// For example:
// HH:MM AM/PM
export const formatTimeV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        hour: "2-digit",
        minute: "2-digit",
    });
};

// Returns formatted string representation of year in date
// For example:
// YYYY
export const formatYearV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("yyyy");
};

// Returns formatted string representation of month in date
// For example:
// MMM
export const formatMonthV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("MMM");
};

// Returns formatted string representation of month with year in date
// For example:
// MMM YYYY
export const formatMonthOfYearV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        month: "short",
        year: "numeric",
    });
};

// Returns formatted string representation of day in date
// For example:
// DD
export const formatDayV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("dd");
};

// Returns formatted string representation of hour in date
// For example:
// HH
export const formatHourV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("hh");
};

// Returns formatted string representation of minute in date
// For example:
// MM
export const formatMinuteV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("mm");
};

// Returns formatted string representation of second in date
// For example:
// SS
export const formatSecondV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("ss");
};

// Returns formatted string representation of millisecond in date
// For example:
// SSS
export const formatMillisecondV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("SSS");
};

// Returns formatted string representation of meridiem in date
// For example:
// AM/PM
export const formatMeridiemV1 = (date: number): string => {
    if (isNil(date)) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("a");
};

// Returns date with switched meridiem as compared to original date
// For example:
// Jan 01, 2020, 12:00 AM to Jan 01, 2020, 12:00 PM
export const switchMeridiemV1 = (date: number): number => {
    if (isNil(date)) {
        return -1;
    }

    const originalDate = DateTime.fromMillis(date);
    // Subtract 12 hours from original date
    let switchedDate = originalDate.minus({ hour: 12 });
    // Verify only meridiem changed and not day
    if (switchedDate.day !== originalDate.day) {
        // Day changed, add 12 hours instead
        switchedDate = originalDate.plus({ hour: 12 });
    }

    return switchedDate.toMillis();
};
