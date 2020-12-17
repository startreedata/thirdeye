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
    } else if ((durationInUnits = round(duration.as("months"), 1)) >= 1) {
        // Duration in months
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.month-lowercase")
                : i18n.t("label.months-lowercase")
        }`;
    } else if ((durationInUnits = round(duration.as("weeks"), 1)) >= 1) {
        // Duration in weeks
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.week-lowercase")
                : i18n.t("label.weeks-lowercase")
        }`;
    } else if ((durationInUnits = round(duration.as("days"), 1)) >= 1) {
        // Duration in days
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.day-lowercase")
                : i18n.t("label.days-lowercase")
        }`;
    } else if ((durationInUnits = round(duration.as("hours"), 1)) >= 1) {
        // Duration in hours
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.hour-lowercase")
                : i18n.t("label.hours-lowercase")
        }`;
    } else if ((durationInUnits = round(duration.as("minutes"), 1)) >= 1) {
        // Duration in minutes
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.minute-lowercase")
                : i18n.t("label.minutes-lowercase")
        }`;
    } else if ((durationInUnits = round(duration.as("seconds"), 1)) >= 1) {
        // Duration in seconds
        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.second-lowercase")
                : i18n.t("label.seconds-lowercase")
        }`;
    } else {
        // Duration in milliseconds
        durationInUnits = round(duration.as("milliseconds"), 1);

        return `${formatNumber(durationInUnits, 1)} ${
            durationInUnits === 1
                ? i18n.t("label.millisecond-lowercase")
                : i18n.t("label.milliseconds-lowercase")
        }`;
    }
};

// Returns long formatted string representation of given date
// For example:
// MMM DD, YY, HH:MM AM/PM
export const formatLongDateAndTime = (date: number): string => {
    if (!date) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        month: "short",
        day: "2-digit",
        year: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });
};

// Returns long formatted string representation of date part of given date
// For example:
// MMM DD, YYYY
export const formatLongDate = (date: number): string => {
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

// Returns long formatted string representation of time part of given date
// For example:
// HH:MM AM/PM
export const formatLongTime = (date: number): string => {
    if (!date) {
        return "";
    }

    const dateTime = DateTime.fromMillis(date);

    return dateTime.toLocaleString({
        hour: "2-digit",
        minute: "2-digit",
    });
};
