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
import { QuickScheduleOption } from "../../components/cron-editor/cron-editor.interfaces";

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
