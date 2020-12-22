import i18n from "i18next";
import { round } from "lodash";
import { DateTime, Interval } from "luxon";
import { formatNumber } from "../number-util/number-util";

// Returns most appropriate formatted string representation of interval between given start and
// end time
// For example:
// 1 second
// 1 day
// 1 month and so on
export const formatDuration = (startTime: number, endTime: number): string => {
    if (!startTime || !endTime) {
        return "";
    }

    const duration = Interval.fromDateTimes(
        DateTime.fromMillis(startTime),
        DateTime.fromMillis(endTime)
    ).toDuration();

    let durationInUnits;
    if ((durationInUnits = round(duration.as("years"), 1)) >= 1) {
        // Duration in years
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.year-lowercase")
                : i18n.t("label.years-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("months"), 1)) >= 1) {
        // Duration in months
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.month-lowercase")
                : i18n.t("label.months-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("weeks"), 1)) >= 1) {
        // Duration in weeks
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.week-lowercase")
                : i18n.t("label.weeks-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("days"), 1)) >= 1) {
        // Duration in days
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.day-lowercase")
                : i18n.t("label.days-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("hours"), 1)) >= 1) {
        // Duration in hours
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.hour-lowercase")
                : i18n.t("label.hours-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("minutes"), 1)) >= 1) {
        // Duration in minutes
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.minute-lowercase")
                : i18n.t("label.minutes-lowercase")
        }`;
    }

    if ((durationInUnits = round(duration.as("seconds"), 1)) >= 1) {
        // Duration in seconds
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.second-lowercase")
                : i18n.t("label.seconds-lowercase")
        }`;
    }

    // Duration in milliseconds
    durationInUnits = round(duration.as("milliseconds"), 1);

    return `${formatNumber(durationInUnits, 1)} ${
        durationInUnits === 1
            ? i18n.t("label.millisecond-lowercase")
            : i18n.t("label.milliseconds-lowercase")
    }`;
};

// Returns formatted string representation of given date
// For example:
// MMM DD, YYYY, HH:MM AM/PM
export const formatDateAndTime = (date: number): string => {
    if (!date) {
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

// Returns formatted string representation of date part in given date
// For example:
// MMM DD, YYYY
export const formatDate = (date: number): string => {
    if (!date) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        month: "short",
        day: "2-digit",
        year: "numeric",
    });
};

// Returns formatted string representation of time part in given date
// For example:
// HH:MM AM/PM
export const formatTime = (date: number): string => {
    if (!date) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        hour: "2-digit",
        minute: "2-digit",
    });
};

// Returns formatted string representation of year in given date
// For example:
// YYYY
export const formatYear = (date: number): string => {
    if (!date) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("yyyy");
};

// Returns formatted string representation of month in given date
// For example:
// MMM
export const formatMonth = (date: number): string => {
    if (!date) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("MMM");
};

// Returns formatted string representation of month with year in given date
// For example:
// MMM YYYY
export const formatMonthOfYear = (date: number): string => {
    if (!date) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        month: "short",
        year: "numeric",
    });
};

// Returns formatted string representation of day in given date
// For example:
// DD
export const formatDay = (date: number): string => {
    if (!date) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("dd");
};

// Returns formatted string representation of hour in given date
// For example:
// HH
export const formatHour = (date: number): string => {
    if (!date) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("hh");
};

// Returns formatted string representation of minute in given date
// For example:
// MM
export const formatMinute = (date: number): string => {
    if (!date) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("mm");
};

// Returns formatted string representation of meridiem in given date
// For example:
// AM/PM
export const formatMeridiem = (date: number): string => {
    if (!date) {
        return "";
    }

    return DateTime.fromMillis(date).toFormat("a");
};

// Returns date with switched meridiem as compared to given date
// For example:
// Dec 01, 20, 12:00 PM to Dec 01, 20, 12:00 AM
export const switchMeridiem = (date: number): number => {
    if (!date) {
        return -1;
    }
    const originalDate = DateTime.fromMillis(date);

    // Subtract 12 hours from original date
    let switchedDate = originalDate.minus({ hour: 12 });

    // Verify only meridiem changed and not day
    if (switchedDate.day !== originalDate.day) {
        // Date changed, add 12 hours instead
        switchedDate = originalDate.plus({ hour: 12 });
    }

    return switchedDate.toMillis();
};
