import i18n from "i18next";
import { DateTime, Interval } from "luxon/";

// Returns the interval between given start and end time formatted as the most appropriate duration
// For example 1 second, 1 day, 1 month etc.
export const formatDuration = (startTime: number, endTime: number): string => {
    const duration = Interval.fromDateTimes(
        new Date(startTime),
        new Date(endTime)
    ).toDuration();

    if (duration.as("years") >= 1) {
        // Duration in years
        return `${duration.as("years")} ${
            duration.as("years") === 1
                ? i18n.t("label.year-lowercase")
                : i18n.t("label.years-lowercase")
        }`;
    } else if (duration.as("months") >= 1) {
        // Duration in months
        return `${duration.as("months")} ${
            duration.as("months") === 1
                ? i18n.t("label.month-lowercase")
                : i18n.t("label.months-lowercase")
        }`;
    } else if (duration.as("weeks") >= 1) {
        // Duration in weeks
        return `${duration.as("weeks")} ${
            duration.as("weeks") === 1
                ? i18n.t("label.week-lowercase")
                : i18n.t("label.weeks-lowercase")
        }`;
    } else if (duration.as("days") >= 1) {
        // Duration in days
        return `${duration.as("days")} ${
            duration.as("days") === 1
                ? i18n.t("label.day-lowercase")
                : i18n.t("label.days-lowercase")
        }`;
    } else if (duration.as("hours") >= 1) {
        // Duration in hours
        return `${duration.as("hours")} ${
            duration.as("hours") === 1
                ? i18n.t("label.hour-lowercase")
                : i18n.t("label.hours-lowercase")
        }`;
    } else if (duration.as("minutes") >= 1) {
        // Duration in minutes
        return `${duration.as("minutes")} ${
            duration.as("minutes") === 1
                ? i18n.t("label.minute-lowercase")
                : i18n.t("label.minutes-lowercase")
        }`;
    } else if (duration.as("seconds") >= 1) {
        // Duration in seconds
        return `${duration.as("seconds")} ${
            duration.as("seconds") === 1
                ? i18n.t("label.second-lowercase")
                : i18n.t("label.seconds-lowercase")
        }`;
    } else {
        // Duration in milliseconds
        return `${duration.as("milliseconds")} ${
            duration.as("milliseconds") === 1
                ? i18n.t("label.millisecond-lowercase")
                : i18n.t("label.milliseconds-lowercase")
        }`;
    }
};

// Returns given date as a formatted string of the form MMM DD, YY, HH:MM AM/PM
export const formatLongDateAndTime = (date: number): string => {
    const dateTime = DateTime.fromJSDate(new Date(date));

    return dateTime.toLocaleString({
        month: "short",
        day: "2-digit",
        year: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });
};
