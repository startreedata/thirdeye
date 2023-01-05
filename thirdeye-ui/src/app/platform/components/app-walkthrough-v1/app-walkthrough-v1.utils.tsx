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
import { Box, Card, CardContent, Chip, Typography } from "@material-ui/core";
import type { StepType } from "@reactour/tour";
import React, { ReactNode } from "react";

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
    ANOMALY_FEEDBACK_CHART_AREA: `${TOURS.RCA}ANOMALY_FEEDBACK_CHART_AREA`,
    ANOMALY_FEEDBACK_DROPDOWN: `${TOURS.RCA}__ANOMALY_FEEDBACK_DROPDOWN`,
    ANOMALY_FEEDBACK_COMMENT: `${TOURS.RCA}__ANOMALY_FEEDBACK_COMMENT`,
    CHANGE_START_END_RANGE: `${TOURS.RCA}__CHANGE_START_END_RANGE`,
    CHART_LABELS: `${TOURS.RCA}__CHART_LABELS`,
    INVESTIGATE_ANOMALY: `${TOURS.RCA}__INVESTIGATE_ANOMALY`,
    PREVIOUS_INVESTIGATIONS: `${TOURS.RCA}__PREVIOUS_INVESTIGATIONS`,
} as const;

// TODO: Move to a generic type utils file
export type ValueOf<T> = T[keyof T];

// TODO: Use generic Tour IDs
export type StepIdType = ValueOf<typeof RCA_TOUR_IDS>;

export interface ExtendedStepType extends StepType {
    id: StepIdType;
    nextLabel?: string;
    disableNext?: boolean; // Disable manual "next" navigation. Useful if the user needs to interact
}

const getContentElement = (
    params: string | { title?: string; body?: string | ReactNode }
): ReactNode => {
    if (typeof params === "string") {
        return <Typography variant="body2">{params}</Typography>;
    }

    return (
        <Box>
            {params.title ? (
                <Typography paragraph variant="h5">
                    {params.title}
                </Typography>
            ) : null}
            {params.body ? (
                <Typography paragraph={false} variant="body2">
                    {params.body}
                </Typography>
            ) : null}
        </Box>
    );
};

export const RcaTourSteps: ExtendedStepType[] = (
    [
        {
            // No selector needed for this first one
            content: getContentElement({
                title: "ThirdEye Root-cause In-App guide",
                body: "Guided tour for investigating an anomaly and doing a root-cause analysis",
            }),
            position: "center",
            nextLabel: "Start Tour",
        },
        {
            selector: RCA_TOUR_IDS.ANOMALY_HEADER_TEXT,
            content: getContentElement({
                title: "Anomaly Page Header",
                body: (
                    <>
                        The anomaly name is typically shown as <br />
                        <Card elevation={0} variant="outlined">
                            <CardContent>
                                <Chip
                                    color="primary"
                                    label="name of parent alert"
                                    size="small"
                                    variant="default"
                                />
                                : Anomaly #
                                <Chip
                                    label="anomaly id"
                                    size="small"
                                    variant="default"
                                />
                            </CardContent>
                        </Card>
                    </>
                ),
            }),
        },
        {
            selector: RCA_TOUR_IDS.ANOMALY_START_END_DATE_TEXT,
            content: getContentElement({
                title: "Anomaly Duration",
                body: "These three fields in the Anomaly Card show the Start Date, the End Date, and the corresponding Duration when the anomalous activity was detected",
            }),
        },
        {
            selector: RCA_TOUR_IDS.CURRENT_PREDICTED_DEVIATION,
            content: getContentElement({
                title: "Deviation from prediction",
                body: "These two fields in the Anomaly Card show how much the actual anomalous behaviour deviated from the system's prediction, in absolute and relative terms",
            }),
        },
        {
            selector: RCA_TOUR_IDS.ANOMALY_FEEDBACK_CHART_AREA,
            content: getContentElement({
                title: "Anomaly chart",
                body: "This is the chart for the detected anomaly, highlighted and plotted against the expected behavior",
            }),
            position: "top",
        },
        {
            selector: RCA_TOUR_IDS.ANOMALY_FEEDBACK_DROPDOWN,
            content: getContentElement({
                title: "Submit feedback",
                body: "After reviewing the detected anomaly, you can submit feedback about the type and accuracy of this detection. This will help the system improve its \
                detection capabilities and ignore false positive",
            }),
            position: "top",
        },
        {
            selector: RCA_TOUR_IDS.ANOMALY_FEEDBACK_COMMENT,
            content: getContentElement({
                title: "Add a comment",
                body: "You can also add a text comment to the anomaly to better explain the selected feedback, add context, or jot down notes",
            }),
            position: "top",
        },
        {
            selector: RCA_TOUR_IDS.CHANGE_START_END_RANGE,
            content: getContentElement({
                title: "Start and End Date range",
                body: "The date range for plotting the anomaly chart is auto-selected in proportion to the duration of the anomaly. Though not recommended, this can be changed as needed",
            }),
        },
        {
            selector: RCA_TOUR_IDS.CHART_LABELS,
            content: getContentElement({
                title: "Chart Legend",
                body: (
                    <>
                        The legend shows the following plotted datasets
                        <ul>
                            <li>Recorded Activity</li>
                            <li>System Prediction</li>
                            <li>Detected Anomaly</li>
                            <li>Expected deviation bounds</li>
                        </ul>
                    </>
                ),
            }),
        },
        {
            selector: RCA_TOUR_IDS.INVESTIGATE_ANOMALY,
            content: getContentElement({
                title: "Investigate Anomaly",
                body: "To further explore an Anomaly, click on this button to start an investigation",
            }),
        },
        {
            selector: RCA_TOUR_IDS.PREVIOUS_INVESTIGATIONS,
            content: getContentElement({
                title: "Investigate Anomaly",
                body: "All previous investigations are stored and displayed in this table at the bottom",
            }),
        },
    ] as StepType[]
).map<ExtendedStepType>((step) => ({
    ...step,
    id: step.selector as StepIdType,
}));

export type SelectorType = "tour" | "tour-observe";

export const getTourSelector = (
    p: string,
    selectorType: SelectorType = "tour"
): string => `[data-${selectorType}-id='${p}']`;

export const getSteps = (): ExtendedStepType[] => {
    const selectedSteps = RcaTourSteps;

    return selectedSteps.map((s) => ({
        ...s,
        ...(s.selector && {
            selector: getTourSelector(s.selector as string),
        }),
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
