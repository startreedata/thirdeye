import { every, isNaN, isNumber } from "lodash";

export const DAY_STRING_TO_IDX = [
    "SUN",
    "MON",
    "TUE",
    "WED",
    "THU",
    "FRI",
    "SAT",
];

export enum TimeOfDay {
    AM = "AM",
    PM = "PM",
}

export interface BuildCronStringProps {
    minute?: number;
    hour?: number;
    dayOfWeek: Array<boolean>;
}

/**
 * @param minute - Expected to be from 0-59
 * @param hour - Expected to be from 0-23
 * @param dayOfWeek - Can be 0-7, SUN-SAT, or comma seperated list of them
 */
export const buildCronString = ({
    minute,
    hour,
    dayOfWeek,
}: BuildCronStringProps): string => {
    // Initiate with seconds
    let cronStringParts = ["0"];

    if (isNumber(minute)) {
        cronStringParts.push(minute.toString());
    } else {
        // Default to 0
        cronStringParts.push("0");
    }

    if (isNumber(hour)) {
        cronStringParts.push(hour.toString());
    } else {
        // Default to 0
        cronStringParts.push("0");
    }

    // Push every day of the month and every month
    cronStringParts = [...cronStringParts, "?", "*"];

    if (every(dayOfWeek)) {
        cronStringParts.push("*");
    } else if (dayOfWeek.some((isSelected) => isSelected)) {
        const dayOfWeekParts: string[] = [];

        dayOfWeek.forEach((isSelected, idx) => {
            if (isSelected) {
                dayOfWeekParts.push(DAY_STRING_TO_IDX[idx]);
            }
        });

        if (dayOfWeekParts.length > 0) {
            cronStringParts.push(dayOfWeekParts.join(","));
        } else {
            cronStringParts.push("*");
        }
    } else {
        cronStringParts.push("*");
    }

    // Push every year
    cronStringParts.push("*");

    return cronStringParts.join(" ");
};

export const generateDayOfWeekArray = (
    defaultBool: boolean,
    overrides: {
        SUN?: boolean;
        MON?: boolean;
        TUE?: boolean;
        WED?: boolean;
        THU?: boolean;
        FRI?: boolean;
        SAT?: boolean;
    } = {}
): Array<boolean> => {
    return [
        overrides.SUN ?? defaultBool, // Sunday
        overrides.MON ?? defaultBool, // Monday
        overrides.TUE ?? defaultBool, // Tuesday
        overrides.WED ?? defaultBool, // Wednesday
        overrides.THU ?? defaultBool, // Thursday
        overrides.FRI ?? defaultBool, // Friday
        overrides.SAT ?? defaultBool, // Saturday
    ];
};

export const parseDayCandidate = (candidate: string): number => {
    if (Number(candidate)) {
        return Number(candidate);
    } else {
        return DAY_STRING_TO_IDX.indexOf(candidate.toUpperCase());
    }
};

export const parseCronString = (cronString: string): BuildCronStringProps => {
    const [, minute, hour, , , dayOfWeek] = cronString.split(" ");
    const parsed: BuildCronStringProps = {
        dayOfWeek: generateDayOfWeekArray(false),
    };

    // Expected to be from 0-59
    if (minute) {
        if (!isNaN(Number(minute))) {
            parsed.minute = Number(minute);
        }
    }

    // Expected to be from 0-23
    if (hour) {
        if (!isNaN(Number(hour))) {
            parsed.hour = Number(hour);
        }
    }

    // Can be *, 0-7, SUN-SAT, or comma seperated list of them
    if (dayOfWeek) {
        const selectedDays = generateDayOfWeekArray(false);

        if (dayOfWeek === "*" || dayOfWeek === "?") {
            selectedDays.forEach((_, idx) => {
                selectedDays[idx] = true;
            });
        } else if (dayOfWeek.indexOf(",") > 0) {
            // If comma seperated list
            const commaSeperatedParsed = dayOfWeek.split(",");

            commaSeperatedParsed.forEach((numOrString: string) => {
                selectedDays[parseDayCandidate(numOrString)] = true;
            });
        } else if (dayOfWeek.indexOf("-") > 0) {
            // If range of values
            const [start, end] = dayOfWeek.split("-");
            const idxStart = parseDayCandidate(start);
            const idxEnd = parseDayCandidate(end);

            if (idxEnd > idxStart) {
                for (
                    let currentDay = idxStart;
                    currentDay <= idxEnd;
                    currentDay++
                ) {
                    selectedDays[currentDay] = true;
                }
            }
        } else {
            // Assume single day
            selectedDays[parseDayCandidate(dayOfWeek)] = true;
        }

        parsed.dayOfWeek = selectedDays;
    }

    return parsed;
};

const ALL = ["*", "?"];

export const isSimpleConvertible = (cronString: string): boolean => {
    const [second, minute, hour, dayOfMonth, month, dayOfWeek, year] =
        cronString.split(" ");

    // If the parts not included in simple mode is not "all"
    if (
        ALL.indexOf(dayOfMonth) === -1 ||
        ALL.indexOf(month) === -1 ||
        ALL.indexOf(year) === -1
    ) {
        return false;
    }

    if (second !== "0") {
        return false;
    }

    if (isNaN(Number(minute)) || isNaN(Number(hour))) {
        return false;
    }

    if (dayOfWeek.indexOf("/") > -1) {
        return false;
    }

    return true;
};
