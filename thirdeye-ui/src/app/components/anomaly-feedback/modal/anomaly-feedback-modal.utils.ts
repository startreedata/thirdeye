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

import { AnomalyCause } from "../../../rest/dto/anomaly.interfaces";
import { AnomalyFeedbackReasonOption } from "../anomaly-feedback.interfaces";

export const REASONS: AnomalyFeedbackReasonOption[] = [
    {
        label: "Holiday effect",
        serverValue: AnomalyCause.HOLIDAY_EFFECT,
    },
    {
        label: "User error",
        serverValue: AnomalyCause.USER_ERROR,
    },
    {
        label: "Software failure",
        serverValue: AnomalyCause.SOFTWARE_FAILURE,
    },
    {
        label: "Environmental factors (like Storm, EQ ... )",
        serverValue: AnomalyCause.ENVIRONMENTAL_FACTOR,
    },
    {
        label: "Fraudulent activities",
        serverValue: AnomalyCause.FRAUD,
    },
    {
        label: "Platform upgrades",
        serverValue: AnomalyCause.PLATFORM_UPGRADE,
    },
    {
        label: "External events (Ex: Campaigns, Price changes etc)",
        serverValue: AnomalyCause.EXTERNAL_EVENT,
    },
    {
        label: "Others",
        serverValue: AnomalyCause.OTHER,
    },
];
