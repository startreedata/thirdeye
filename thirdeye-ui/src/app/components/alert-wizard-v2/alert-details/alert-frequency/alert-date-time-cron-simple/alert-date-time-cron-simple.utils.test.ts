/*
 * Copyright 2022 StarTree Inc
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
import {
    buildCronString,
    generateDayOfWeekArray,
    isSimpleConvertible,
    parseCronString,
    parseDayCandidate,
} from "./alert-date-time-cron-simple.utils";

describe("Alert Date Time Cron Simple Utils", () => {
    it("buildCronString should output * if all days are selected", () => {
        expect(
            buildCronString({
                minute: 10,
                hour: 10,
                dayOfWeek: [true, true, true, true, true, true, true],
            })
        ).toEqual("0 10 10 ? * * *");
    });

    it("buildCronString should output comma operated string for days if not all days are selected", () => {
        expect(
            buildCronString({
                minute: 59,
                hour: 0,
                dayOfWeek: [true, true, true, true, false, true, true],
            })
        ).toEqual("0 59 0 ? * SUN,MON,TUE,WED,FRI,SAT *");
    });

    it("buildCronString should output 0 for hour and minute if they are undefined", () => {
        expect(
            buildCronString({
                dayOfWeek: [true, true, true, true, true, true, true],
            })
        ).toEqual("0 0 0 ? * * *");
    });

    it("parseDayCandidate should output correct indices for strings", () => {
        expect(parseDayCandidate("SAT")).toEqual(6);
        expect(parseDayCandidate("MON")).toEqual(1);
        expect(parseDayCandidate("1")).toEqual(1);
        expect(parseDayCandidate("4")).toEqual(4);
    });

    it("generateDayOfWeekArray should output correct array of booleans depending on options", () => {
        expect(generateDayOfWeekArray(false)).toEqual([
            false,
            false,
            false,
            false,
            false,
            false,
            false,
        ]);
        expect(generateDayOfWeekArray(false, { WED: true })).toEqual([
            false,
            false,
            false,
            true,
            false,
            false,
            false,
        ]);
    });

    it("parseCronString should output correct BuildCronStringProps object", () => {
        expect(parseCronString("0 15 10 ? * * *")).toEqual({
            dayOfWeek: [true, true, true, true, true, true, true],
            hour: 10,
            minute: 15,
        });
        expect(parseCronString("0 15 10 ? * ? *")).toEqual({
            dayOfWeek: [true, true, true, true, true, true, true],
            hour: 10,
            minute: 15,
        });
        expect(parseCronString("0 15 10 ? * MON *")).toEqual({
            dayOfWeek: [false, true, false, false, false, false, false],
            hour: 10,
            minute: 15,
        });
        expect(parseCronString("0 15 10 ? * MON-THU *")).toEqual({
            dayOfWeek: [false, true, true, true, true, false, false],
            hour: 10,
            minute: 15,
        });
        expect(parseCronString("0 15 10 ? * MON,THU *")).toEqual({
            dayOfWeek: [false, true, false, false, true, false, false],
            hour: 10,
            minute: 15,
        });
        expect(parseCronString("0 0 0 ? * MON-FRI *")).toEqual({
            dayOfWeek: [false, true, true, true, true, true, false],
            hour: 0,
            minute: 0,
        });
    });

    it("isSimpleConvertible should output correct boolean", () => {
        expect(isSimpleConvertible("0 0 5 ? * MON-FRI *")).toEqual(true);
        expect(isSimpleConvertible("0 0 ? ? * MON-FRI *")).toEqual(false);
        expect(isSimpleConvertible("0 0 ? ? * MON-FRI 2005")).toEqual(false);
    });
});
