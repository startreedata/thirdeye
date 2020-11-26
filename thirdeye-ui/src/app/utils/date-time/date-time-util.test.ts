import i18n from "i18next";
import { DateTime, Interval } from "luxon/";
import { formatDuration, formatLongDateAndTime } from "./date-time-util";

jest.mock("i18next");
jest.mock("luxon");

const mockDuration = {
    as: jest.fn(),
};
const mockInterval = {
    toDuration: jest.fn().mockReturnValue(mockDuration),
};
const mockDateTime = {
    toLocaleString: jest.fn().mockReturnValue("testDateTimeFormat"),
};

describe("DateTime Util", () => {
    beforeAll(() => {
        Interval.fromDateTimes = jest.fn().mockReturnValue(mockInterval);
        DateTime.fromJSDate = jest.fn().mockReturnValue(mockDateTime);
        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("formatDuration shall invoke Interval.fromDateTimes with appropriate input", () => {
        formatDuration(1, 2);

        expect(Interval.fromDateTimes).toHaveBeenCalledWith(
            new Date(1),
            new Date(2)
        );
    });

    test("formatDuration shall return appropriate duration in year", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "years") {
                return 1;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("1 label.year-lowercase");
    });

    test("formatDuration shall return appropriate duration in years", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "years") {
                return 2;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("2 label.years-lowercase");
    });

    test("formatDuration shall return appropriate duration in month", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "months") {
                return 1;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("1 label.month-lowercase");
    });

    test("formatDuration shall return appropriate duration in months", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "months") {
                return 2;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("2 label.months-lowercase");
    });

    test("formatDuration shall return appropriate duration in week", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "weeks") {
                return 1;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("1 label.week-lowercase");
    });

    test("formatDuration shall return appropriate duration in weeks", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "weeks") {
                return 2;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("2 label.weeks-lowercase");
    });

    test("formatDuration shall return appropriate duration in day", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "days") {
                return 1;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("1 label.day-lowercase");
    });

    test("formatDuration shall return appropriate duration in days", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "days") {
                return 2;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("2 label.days-lowercase");
    });

    test("formatDuration shall return appropriate duration in hour", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "hours") {
                return 1;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("1 label.hour-lowercase");
    });

    test("formatDuration shall return appropriate duration in hours", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "hours") {
                return 2;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("2 label.hours-lowercase");
    });

    test("formatDuration shall return appropriate duration in minute", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "minutes") {
                return 1;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("1 label.minute-lowercase");
    });

    test("formatDuration shall return appropriate duration in minutes", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "minutes") {
                return 2;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("2 label.minutes-lowercase");
    });

    test("formatDuration shall return appropriate duration in second", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "seconds") {
                return 1;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("1 label.second-lowercase");
    });

    test("formatDuration shall return appropriate duration in seconds", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "seconds") {
                return 2;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("2 label.seconds-lowercase");
    });

    test("formatDuration shall return appropriate duration in millisecond", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "milliseconds") {
                return 1;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("1 label.millisecond-lowercase");
    });

    test("formatDuration shall return appropriate duration in milliseconds", () => {
        mockDuration.as.mockImplementation((unit: string): number => {
            if (unit === "milliseconds") {
                return 2;
            }

            return 0;
        });

        const durationString = formatDuration(1, 2);

        expect(durationString).toEqual("2 label.milliseconds-lowercase");
    });

    test("formatLongDateAndTime shall invoke DateTime.fromJSDate with appropriate input and return result", () => {
        const dateTimeString = formatLongDateAndTime(1);

        expect(DateTime.fromJSDate).toHaveBeenLastCalledWith(new Date(1));
        expect(mockDateTime.toLocaleString).toHaveBeenCalledWith({
            month: "short",
            day: "2-digit",
            year: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
        });
        expect(dateTimeString).toEqual("testDateTimeFormat");
    });
});
