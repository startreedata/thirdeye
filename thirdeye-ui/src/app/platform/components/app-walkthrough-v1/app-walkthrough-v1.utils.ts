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
import type { StepType } from "@reactour/tour";

export const HOMEPAGE_TOUR_IDS = {
    KPI_SECTION: "HOMEPAGE_TOUR_IDS__KPI_SECTION",
    RECENT_ANOMALIES_CHART: "HOMEPAGE_TOUR_IDS__RECENT_ANOMALIES_CHART",
    RECENT_ANOMALIES_DROPDOWN: "HOMEPAGE_TOUR_IDS__RECENT_ANOMALIES_DROPDOWN",
    LATEST_ANOMALIES_TABLE: "HOMEPAGE_TOUR_IDS__LATEST_ANOMALIES_TABLE",
} as const;

export const HomepageTourSteps: StepType[] = [
    {
        selector: HOMEPAGE_TOUR_IDS.KPI_SECTION,
        content: "Here are the KPIs for your monitored trends",
    },
    {
        selector: HOMEPAGE_TOUR_IDS.RECENT_ANOMALIES_CHART,
        content: "Check out the recent anomalies here",
    },
    {
        selector: HOMEPAGE_TOUR_IDS.RECENT_ANOMALIES_DROPDOWN,
        content: "Choose a different time range for the recent anomalies graph",
    },
    {
        selector: HOMEPAGE_TOUR_IDS.LATEST_ANOMALIES_TABLE,
        content: "More comprehensive information about recorded anomalies",
        position: "top",
    },
];
