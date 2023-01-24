/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { DateTime, Duration, Interval } from "luxon";
import { SECOND_IN_MILLISECONDS } from "../../../../../utils/time/time.util";
import { TimeInterval } from "./time-intervals.interfaces";

/**
 * This module is a rewrite of https://github.com/d3/d3-time/blob/master/src/interval.js#L3
 * minus some properties not used. Using luxon DateTime instead of Date in
 * order to account for Timezone
 */
export const timeInterval = (
    floori: (d: DateTime) => DateTime,
    offseti: (d: DateTime, step: number) => DateTime,
    count?: (start: DateTime, end: DateTime) => number,
    field?: (d: DateTime) => number
): TimeInterval => {
    let t0;
    let t1;

    const interval = (date: DateTime): DateTime => {
        return floori(date);
    };

    interval.floor = (date: DateTime): DateTime => {
        return floori(date);
    };

    interval.ceil = (date: DateTime): DateTime => {
        let modifiedDate = floori(date.minus(1));
        modifiedDate = offseti(modifiedDate, 1);
        modifiedDate = floori(modifiedDate);

        return modifiedDate;
    };

    interval.offset = (date: DateTime, step: number) => {
        return offseti(date, step == null ? 1 : Math.floor(step));
    };

    interval.range = (start: DateTime, stop: DateTime, step = 1) => {
        const range: DateTime[] = [];

        start = interval.ceil(start);

        if (start > stop || step <= 0) {
            return range;
        }

        let current = start;

        do {
            range.push(current);
            current = offseti(current, step);
            current = floori(current);
        } while (current < stop);

        return range;
    };

    interval.filter = (testFunc: (date: DateTime) => boolean) => {
        return timeInterval(
            (dateToFloor: DateTime): DateTime => {
                let currentDate = dateToFloor;
                if (dateToFloor >= currentDate) {
                    while (!testFunc(currentDate)) {
                        currentDate = currentDate.minus(1);
                        currentDate = floori(currentDate);
                    }
                }

                return currentDate;
            },
            (dateToOffset, step): DateTime => {
                let currentDate = dateToOffset;

                if (dateToOffset >= currentDate) {
                    if (step < 0) {
                        while (++step <= 0) {
                            do {
                                currentDate = offseti(currentDate, -1);
                            } while (!testFunc(currentDate));
                        }
                    } else {
                        while (--step >= 0) {
                            do {
                                currentDate = offseti(currentDate, +1);
                            } while (!testFunc(currentDate));
                        }
                    }
                }

                return currentDate;
            }
        );
    };

    if (count) {
        interval.count = (start: DateTime, end: DateTime) => {
            t0 = floori(start);
            t1 = floori(end);

            return Math.floor(count(t0, t1));
        };

        interval.every = (step: number) => {
            step = Math.floor(step);

            if (!(step > 1)) {
                return interval;
            }

            if (field) {
                return interval.filter((d) => field(d) % step === 0);
            } else {
                return interval.filter(
                    (d) =>
                        interval.count(DateTime.fromMillis(0), d) % step === 0
                );
            }
        };
    }

    return interval;
};

export const timeMillisecond = timeInterval(
    (date) => {
        // noop
        return date;
    },
    (date, step) => {
        return date.plus(Duration.fromMillis(step));
    },
    (start, end) => {
        return end.toMillis() - start.toMillis();
    }
);

export const timeSecond = timeInterval(
    (date) => {
        return date.startOf("second");
    },
    (date, step) => {
        return date.plus(Duration.fromMillis(step * SECOND_IN_MILLISECONDS));
    },
    (start, end) => {
        return Interval.fromDateTimes(start, end).count("second");
    },
    (date) => {
        return date.second;
    }
);

export const timeMinute = timeInterval(
    (date) => {
        return date.startOf("minute");
    },
    (date, step) => {
        return date.plus({ minute: step });
    },
    (start, end) => {
        return Interval.fromDateTimes(start, end).count("minute");
    },
    (date) => {
        return date.minute;
    }
);

export const timeHour = timeInterval(
    (date) => {
        return date.startOf("hour");
    },
    (date, step) => {
        return date.plus({ hour: step });
    },
    (start, end) => {
        return Interval.fromDateTimes(start, end).count("hour");
    },
    (date) => {
        return date.hour;
    }
);

export const timeDay = timeInterval(
    (date) => {
        return date.startOf("day");
    },
    (date, step) => {
        return date.plus({ days: step });
    },
    (start: DateTime, end: DateTime) => {
        return Interval.fromDateTimes(start, end).count("day");
    },

    (date) => date.daysInMonth - 1
);

export const timeWeekday = (i: number): TimeInterval => {
    return timeInterval(
        (date) => {
            return date.set({
                hour: 0,
                minute: 0,
                second: 0,
                millisecond: 0,
                day: date.day - ((date.day + 7 - i) % 7),
            });
        },
        (date, step) => {
            return date.plus(Duration.fromObject({ week: step }));
        },
        (start, end) => {
            return Interval.fromDateTimes(start, end).count("weeks");
        }
    );
};

export const timeMonth = timeInterval(
    (date) => {
        return date.startOf("month");
    },
    (date, step) => {
        return date.plus(Duration.fromObject({ month: step }));
    },
    (start, end) => {
        return Interval.fromDateTimes(start, end).count("month");
    },
    (date) => {
        return date.month;
    }
);

export const timeYear = timeInterval(
    (date) => {
        return date.startOf("year");
    },
    (date, step) => {
        return date.plus(Duration.fromObject({ year: step }));
    },
    (start, end) => {
        return Interval.fromDateTimes(start, end).count("year");
    },
    (date) => {
        return date.year;
    }
);
