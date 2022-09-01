/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import CronValidator from "cron-expression-validator";
import { last } from "lodash";
import {
    CronIndex,
    QuickScheduleOption,
} from "../../components/cron-editor/cron-editor.interfaces";

export const QuickScheduleOptions: Array<QuickScheduleOption> = [
    {
        label: "Hourly",
        key: "hourly",
        value: "0 * * * *",
    },
    {
        label: "Monthly",
        key: "monthly",
        value: "0 0 1 * *",
    },
    {
        label: "Weekly",
        key: "weekly",
        value: "0 0 * * 0",
    },
    {
        label: "Yearly",
        key: "yearly",
        value: "0 0 1 1 *",
    },
    {
        label: "Daily",
        key: "daily",
        value: "0 0 * * *",
    },
];

export const ClockTimeOptions: Array<string> = ["am", "pm"];

export const CronRepeatOptions: Array<string> = ["day", "month", "year"];

export const convertTime12to24 = (hours: number, modifier: string): number => {
    if (hours === 12) {
        hours = 0;
    }

    if (modifier === "pm") {
        hours = hours + 12;
    }

    return hours;
};

const cronIndex: CronIndex = {
    minute: 1,
    hour: 2,
    day: 3,
    month: 4,
    year: 6,
};

export const getCronValue = (cron: string, type: keyof CronIndex): number => {
    const index = cronIndex[type];

    const defaultValue = type === "month" ? 1 : 0;

    if (CronValidator.isValidCronExpression(cron)) {
        const cronArray = cron.split(" ")[index];
        if (!isNaN(+cronArray)) {
            return +cronArray;
        }
        if (cronArray.includes("*") && cronArray.length === 1) {
            return defaultValue;
        }

        return +(last(cronArray.split("/")) || defaultValue);
    }

    return defaultValue;
};

export const getCronHour = (cron: string): number => {
    const hour = getCronValue(cron, "hour");

    return hour % 12;
};

export const getHourFormat = (cron: string): string => {
    return getCronValue(cron, "hour") <= 11 ? "am" : "pm";
};
