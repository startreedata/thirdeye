import i18n from "i18next";
import { DateTime, Interval } from "luxon/";

// Returns formatted, most appropriate string representation of interval between given start and
// end time
// For example:
// 1 second
// 1 day
// 1 month and so on
export const formatDuration = (startTime: number, endTime: number): string => {
    const duration = Interval.fromDateTimes(
        new Date(startTime),
        new Date(endTime)
    ).toDuration();

    let durationInUnits;
    if ((durationInUnits = duration.as("years")) >= 1) {
        // Duration in years
        return `${durationInUnits} ${
            durationInUnits === 1
                ? i18n.t("label.year-lowercase")
                : i18n.t("label.years-lowercase")
        }`;
    } else if ((durationInUnits = duration.as("months")) >= 1) {
        // Duration in months
        return `${durationInUnits} ${
            durationInUnits === 1
                ? i18n.t("label.month-lowercase")
                : i18n.t("label.months-lowercase")
        }`;
    } else if ((durationInUnits = duration.as("weeks")) >= 1) {
        // Duration in weeks
        return `${durationInUnits} ${
            durationInUnits === 1
                ? i18n.t("label.week-lowercase")
                : i18n.t("label.weeks-lowercase")
        }`;
    } else if ((durationInUnits = duration.as("days")) >= 1) {
        // Duration in days
        return `${durationInUnits} ${
            durationInUnits === 1
                ? i18n.t("label.day-lowercase")
                : i18n.t("label.days-lowercase")
        }`;
    } else if ((durationInUnits = duration.as("hours")) >= 1) {
        // Duration in hours
        return `${durationInUnits} ${
            durationInUnits === 1
                ? i18n.t("label.hour-lowercase")
                : i18n.t("label.hours-lowercase")
        }`;
    } else if ((durationInUnits = duration.as("minutes")) >= 1) {
        // Duration in minutes
        return `${durationInUnits} ${
            durationInUnits === 1
                ? i18n.t("label.minute-lowercase")
                : i18n.t("label.minutes-lowercase")
        }`;
    } else if ((durationInUnits = duration.as("seconds")) >= 1) {
        // Duration in seconds
        return `${durationInUnits} ${
            durationInUnits === 1
                ? i18n.t("label.second-lowercase")
                : i18n.t("label.seconds-lowercase")
        }`;
    } else {
        // Duration in milliseconds
        durationInUnits = duration.as("milliseconds");

        return `${durationInUnits} ${
            durationInUnits === 1
                ? i18n.t("label.millisecond-lowercase")
                : i18n.t("label.milliseconds-lowercase")
        }`;
    }
};

// Returns long, formatted, string representation of given date
// For example:
// MMM DD, YY, HH:MM AM/PM
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
