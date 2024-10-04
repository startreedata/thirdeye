/*
 * Copyright 2024 StarTree Inc
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
import { Alert } from "../../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../../rest/dto/anomaly.interfaces";

export interface ApiReuqestProps {
    selectedAlert: { id: number; name: string };
    defaultAlertDropdownOption: { id: number; name: string };
    selectedAnalysisPeriod: string;
}

export interface APIRequestData {
    alerts: Alert[] | null;
    currentPeriodAnomaliesByAlert: Anomaly[] | null;
    previousPeriodAnomaliesByAlert: Anomaly[] | null;
}
