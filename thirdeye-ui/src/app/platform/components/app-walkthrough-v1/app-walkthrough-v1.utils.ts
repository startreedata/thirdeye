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

export const TOURS = {
    RCA: "RCA",
} as const;

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

// TODO: Is this naming convention fine?
export const RCA_TOUR_IDS = {
    ANOMALY_HEADER_TEXT: `${TOURS.RCA}__ANOMALY_HEADER_TEXT`,
    ANOMALY_START_END_DATE_TEXT: `${TOURS.RCA}__ANOMALY_START_END_DATE_TEXT`,
    CURRENT_PREDICTED_DEVIATION: `${TOURS.RCA}__CURRENT_PREDICTED_DEVIATION`,
    ANOMALY_FEEDBACK_DROPDOWN: `${TOURS.RCA}__ANOMALY_FEEDBACK_DROPDOWN`,
    ANOMALY_FEEDBACK_COMMENT: `${TOURS.RCA}__ANOMALY_FEEDBACK_COMMENT`,
    CHANGE_START_END_RANGE: `${TOURS.RCA}__CHANGE_START_END_RANGE`,
    CHART_LABELS: `${TOURS.RCA}__CHART_LABELS`,
    INVESTIGATE_ANOMALY: `${TOURS.RCA}__INVESTIGATE_ANOMALY`,
} as const;

export const RcaTourSteps: StepType[] = [
    {
        selector: RCA_TOUR_IDS.ANOMALY_HEADER_TEXT,
        content: "ANOMALY_HEADER_TEXT",
    },
    {
        selector: RCA_TOUR_IDS.ANOMALY_START_END_DATE_TEXT,
        content: "ANOMALY_START_END_DATE_TEXT",
    },
    {
        selector: RCA_TOUR_IDS.CURRENT_PREDICTED_DEVIATION,
        content: "CURRENT_PREDICTED_DEVIATION",
    },
    {
        selector: RCA_TOUR_IDS.ANOMALY_FEEDBACK_DROPDOWN,
        content: "ANOMALY_FEEDBACK_DROPDOWN",
        position: "top",
        highlightedSelectors: [RCA_TOUR_IDS.ANOMALY_FEEDBACK_DROPDOWN],
        resizeObservables: [RCA_TOUR_IDS.ANOMALY_FEEDBACK_DROPDOWN],
        mutationObservables: [RCA_TOUR_IDS.ANOMALY_FEEDBACK_DROPDOWN],
    },
    {
        selector: RCA_TOUR_IDS.ANOMALY_FEEDBACK_COMMENT,
        content: "ANOMALY_FEEDBACK_COMMENT",
        position: "top",
        highlightedSelectors: [RCA_TOUR_IDS.ANOMALY_FEEDBACK_COMMENT],
        resizeObservables: [RCA_TOUR_IDS.ANOMALY_FEEDBACK_COMMENT],
        mutationObservables: [RCA_TOUR_IDS.ANOMALY_FEEDBACK_COMMENT],
    },
    // {
    //     selector: RCA_TOUR_IDS.CHANGE_START_END_RANGE,
    //     content: "CHANGE_START_END_RANGE",
    // },
    // {
    //     selector: RCA_TOUR_IDS.CHART_LABELS,
    //     content: "CHART_LABELS",
    // },
    // {
    //     selector: RCA_TOUR_IDS.INVESTIGATE_ANOMALY,
    //     content: "INVESTIGATE_ANOMALY",
    // },
];

export type SelectorType = "tour" | "tour-observe";

const getTourSelector = (
    p: string,
    selectorType: SelectorType = "tour"
): string => `[data-${selectorType}-id='${p}']`;

export const getSteps = (): StepType[] => {
    const selectedSteps = RcaTourSteps;

    return selectedSteps.map((s) => ({
        ...s,
        selector: getTourSelector(s.selector as string),
        ...(s.highlightedSelectors && {
            highlightedSelectors: s.highlightedSelectors.map((v) =>
                getTourSelector(v, "tour-observe")
            ),
        }),
        ...(s.resizeObservables && {
            resizeObservables: s.resizeObservables.map((v) =>
                getTourSelector(v, "tour-observe")
            ),
        }),
        ...(s.mutationObservables && {
            mutationObservables: s.mutationObservables.map((v) =>
                getTourSelector(v, "tour-observe")
            ),
        }),
    }));
};
