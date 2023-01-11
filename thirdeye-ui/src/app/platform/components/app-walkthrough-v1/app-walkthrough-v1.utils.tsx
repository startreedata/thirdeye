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
import { StepType, TourProps, useTour } from "@reactour/tour";
import React, { ReactNode, useEffect, useMemo } from "react";

export const TOURS = {
    RCA_ANOMALY: "RCA_ANOMALY",
    RCA_INVESTIGATE: "RCA_INVESTIGATE",
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
export const RCA_ANOMALY_TOUR_IDS = {
    ANOMALY_HEADER_TEXT: `${TOURS.RCA_ANOMALY}__ANOMALY_HEADER_TEXT`,
    ANOMALY_START_END_DATE_TEXT: `${TOURS.RCA_ANOMALY}__ANOMALY_START_END_DATE_TEXT`,
    CURRENT_PREDICTED_DEVIATION: `${TOURS.RCA_ANOMALY}__CURRENT_PREDICTED_DEVIATION`,
    ANOMALY_FEEDBACK_CHART_AREA: `${TOURS.RCA_ANOMALY}ANOMALY_FEEDBACK_CHART_AREA`,
    ANOMALY_FEEDBACK_DROPDOWN: `${TOURS.RCA_ANOMALY}__ANOMALY_FEEDBACK_DROPDOWN`,
    ANOMALY_FEEDBACK_COMMENT: `${TOURS.RCA_ANOMALY}__ANOMALY_FEEDBACK_COMMENT`,
    CHANGE_START_END_RANGE: `${TOURS.RCA_ANOMALY}__CHANGE_START_END_RANGE`,
    CHART_LABELS: `${TOURS.RCA_ANOMALY}__CHART_LABELS`,
    INVESTIGATE_ANOMALY: `${TOURS.RCA_ANOMALY}__INVESTIGATE_ANOMALY`,
    PREVIOUS_INVESTIGATIONS: `${TOURS.RCA_ANOMALY}__PREVIOUS_INVESTIGATIONS`,
} as const;

export const RCA_INVESTIGATE_TOUR_IDS = {
    HEATMAP_BASELINE_OFFSET: `${TOURS.RCA_INVESTIGATE}__HEATMAP_BASELINE_OFFSET`,
    HEATMAP_DIMENSION_DRILLS: `${TOURS.RCA_INVESTIGATE}__HEATMAP_DIMENSION_DRILLS`,
    HEATMAP_DIMENSION_DRILLS_SELECTED: `${TOURS.RCA_INVESTIGATE}__HEATMAP_DIMENSION_DRILLS_SELECTED`,
    TOP_CONTRIBUTORS: `${TOURS.RCA_INVESTIGATE}__TOP_CONTRIBUTORS`,
    EVENTS_INTRO: `${TOURS.RCA_INVESTIGATE}__EVENTS_INTRO`,
    EVENTS_ADD: `${TOURS.RCA_INVESTIGATE}__EVENTS_ADD`,
    SAVE_INVESTIGATION: `${TOURS.RCA_INVESTIGATE}__SAVE_INVESTIGATION`,
} as const;

export type ValueOf<T> = T[keyof T];

// * Keep adding tour ids here to get ts support across the in-app tour implementation for them
type AllTourKeys = typeof RCA_ANOMALY_TOUR_IDS &
    typeof RCA_INVESTIGATE_TOUR_IDS;

export type StepId<T = AllTourKeys> = ValueOf<T>;

export interface ExtendedStepType<T = AllTourKeys> extends StepType {
    id: StepId<T>;
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

export const transformTourSteps = <T,>(
    stepsList: StepType[]
): ExtendedStepType<T>[] =>
    stepsList.map<ExtendedStepType<T>>((step) => ({
        ...step,
        id: step.selector as StepId<T>,
    }));

export const getRcaAnomalyTourSteps = transformTourSteps<
    typeof RCA_ANOMALY_TOUR_IDS
>([
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
        selector: RCA_ANOMALY_TOUR_IDS.ANOMALY_HEADER_TEXT,
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
        selector: RCA_ANOMALY_TOUR_IDS.ANOMALY_START_END_DATE_TEXT,
        content: getContentElement({
            title: "Anomaly Duration",
            body: "These three fields in the Anomaly Card show the Start Date, the End Date, and the corresponding Duration when the anomalous activity was detected",
        }),
    },
    {
        selector: RCA_ANOMALY_TOUR_IDS.CURRENT_PREDICTED_DEVIATION,
        content: getContentElement({
            title: "Deviation from prediction",
            body: "These two fields in the Anomaly Card show how much the actual anomalous behaviour deviated from the system's prediction, in absolute and relative terms",
        }),
    },
    {
        selector: RCA_ANOMALY_TOUR_IDS.ANOMALY_FEEDBACK_CHART_AREA,
        content: getContentElement({
            title: "Anomaly chart",
            body: "This is the chart for the detected anomaly, highlighted and plotted against the expected behavior",
        }),
        position: "top",
    },
    {
        selector: RCA_ANOMALY_TOUR_IDS.ANOMALY_FEEDBACK_DROPDOWN,
        content: getContentElement({
            title: "Submit feedback",
            body: "After reviewing the detected anomaly, you can submit feedback about the type and accuracy of this detection. This will help the system improve its \
                detection capabilities and ignore false positive",
        }),
        position: "top",
    },
    {
        selector: RCA_ANOMALY_TOUR_IDS.ANOMALY_FEEDBACK_COMMENT,
        content: getContentElement({
            title: "Add a comment",
            body: "You can also add a text comment to the anomaly to better explain the selected feedback, add context, or jot down notes",
        }),
        position: "top",
    },
    {
        selector: RCA_ANOMALY_TOUR_IDS.CHANGE_START_END_RANGE,
        content: getContentElement({
            title: "Start and End Date range",
            body: "The date range for plotting the anomaly chart is auto-selected in proportion to the duration of the anomaly. Though not recommended, this can be changed as needed",
        }),
    },
    {
        selector: RCA_ANOMALY_TOUR_IDS.CHART_LABELS,
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
        selector: RCA_ANOMALY_TOUR_IDS.INVESTIGATE_ANOMALY,
        content: getContentElement({
            title: "Investigate Anomaly",
            body: "To further explore an Anomaly, click on this button to start an investigation",
        }),
    },
    {
        selector: RCA_ANOMALY_TOUR_IDS.PREVIOUS_INVESTIGATIONS,
        content: getContentElement({
            title: "Investigate Anomaly",
            body: "All previous investigations are stored and displayed in this table at the bottom",
        }),
    },
] as StepType[]);

export const getRcaInvestigateTourSteps = transformTourSteps<
    typeof RCA_INVESTIGATE_TOUR_IDS
>([
    {
        selector: RCA_INVESTIGATE_TOUR_IDS.HEATMAP_BASELINE_OFFSET,
        content: getContentElement({
            title: "Baseline Offset",
            body: (
                <ul>
                    <li>
                        Baseline Offset lets you view the impact profile of each
                        dimension across time
                    </li>
                    <li>
                        It helps with comparing the anomaly heatmap with various
                        points back in time (4 days ago, 3 weeks ago, 6 months
                        ago, and so on)
                    </li>
                    <li>
                        It is helpful in distinguishing between a sudden spike
                        and a gentle slope on a granular level, which may elude
                        to the factors that caused the observed anomalous
                        behaviour
                    </li>
                </ul>
            ),
        }),
        position: "top",
    },
    {
        selector: RCA_INVESTIGATE_TOUR_IDS.HEATMAP_DIMENSION_DRILLS,
        content: getContentElement({
            title: "Dimension Drills",
            body: (
                <>
                    <p>
                        This is a breakdown of the possible values for each
                        dimension, sized in proportion to prevalence. Hovering
                        over each value displays a tooltip with more information
                        about the absolute value and the percentage change.
                    </p>
                    <Card elevation={0} variant="outlined">
                        <CardContent>
                            Click on a dimensional value to apply it as a
                            filter. The dimension heatmap will refresh according
                            to the selected filter.
                        </CardContent>
                    </Card>
                    <p>
                        To add the selected value filter to the chart above,
                        click on &nbsp;
                        <Chip
                            color="primary"
                            label="Add to Chart"
                            size="small"
                            style={{ display: "inline-block" }}
                            variant="default"
                        />
                    </p>
                </>
            ),
        }),
        stepInteraction: true,
    },
    {
        selector: RCA_INVESTIGATE_TOUR_IDS.HEATMAP_DIMENSION_DRILLS_SELECTED,
        content: getContentElement({
            title: "Dimension Value - Custom Chart Filter",
            body: "Toggling this filter on can enable a better insight into the isolated effect of this value in the recorded data.",
        }),
    },

    {
        selector: RCA_INVESTIGATE_TOUR_IDS.TOP_CONTRIBUTORS,
        content: getContentElement({
            title: "Top Contributors",
            body: (
                <>
                    This tab shows the list of identified dimension (or a group
                    of dimensions) that have the most contribution to the
                    metrics trend change for a given anomaly, ordered from
                    highest impact to lowest impact.
                </>
            ),
        }),
    },
    {
        selector: RCA_INVESTIGATE_TOUR_IDS.EVENTS_INTRO,
        content: getContentElement({
            title: "Events",
            body: (
                <>
                    <p>
                        Events are specially designated intervals of time where
                        a general abnormal level of activity is expected, like a
                        holiday or a major happening. This tab lets you see if a
                        certain anomaly is correlated with an event, which may
                        help ascertain the root cause for the observed
                        drop/spike.
                    </p>
                    <Card elevation={0} variant="outlined">
                        <CardContent>
                            As an example, if you changed the targeting
                            configuration for a campaign, adding that time
                            period as an event to the investigation might help
                            explain an abnormal frequency of observed anomalies
                            that were unknowingly being caused by that change.
                        </CardContent>
                    </Card>
                    <p>
                        In this example scenario, after reviewing the
                        investigation, restoring that targeting configuration
                        might help reduce/eliminate the observed anomalies.
                    </p>
                </>
            ),
        }),
        position: "top",
    },
    {
        selector: RCA_INVESTIGATE_TOUR_IDS.EVENTS_ADD,
        content: getContentElement({
            title: "Add a Custom Event",
            body: "You can add your own events from the button here.",
        }),
    },
    {
        selector: RCA_INVESTIGATE_TOUR_IDS.SAVE_INVESTIGATION,
        content: getContentElement({
            title: "Save the Investigation",
            body: (
                <>
                    Finally, the investigation can be saved from the button
                    here. This will log the progress so far as an investigation
                    entry in the anomaly page as shown previously, and be
                    available for anyone to examine in the future
                </>
            ),
        }),
    },
] as StepType[]);

export type SelectorType = "tour" | "tour-observe";

export const getTourSelector = (
    p: string,
    selectorType: SelectorType = "tour"
): string => `[data-${selectorType}-id='${p}']`;

export type TourType = keyof typeof TOURS;

const tourStepsMap: Record<
    TourType,
    (
        | ExtendedStepType<typeof RCA_ANOMALY_TOUR_IDS>
        | ExtendedStepType<typeof RCA_INVESTIGATE_TOUR_IDS>
    )[]
> = {
    [TOURS.RCA_ANOMALY]: getRcaAnomalyTourSteps,
    [TOURS.RCA_INVESTIGATE]: getRcaInvestigateTourSteps,
};

export const getTourSteps = <T extends Record<string, unknown>>(
    tourType: TourType,
    stepProps?: T
): ExtendedStepType[] => {
    // TODO: Inject stepProps
    const selectedSteps = tourStepsMap[tourType];

    // TODO: Remove if no better use is found
    stepProps && console.log({ stepProps });

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

type UseAppTourReturn = { startTour: () => void } & TourProps;

// Use this hook throughout the app, as this will expose the minimal interface required
// to use the in-app tour while handling all the common abstraction behind the scenes
export const useAppTour = <T extends Record<string, unknown>>(
    tourType: TourType,
    stepProp?: T
): UseAppTourReturn => {
    const tourContext = useTour();

    const { setIsOpen, setSteps } = tourContext;

    const tourSteps = useMemo(
        () => getTourSteps<T>(tourType, stepProp),
        [stepProp]
    );

    useEffect(() => {
        setSteps(tourSteps);
    }, [tourSteps]);

    const startTour = (): void => {
        setIsOpen(true);
    };

    return { startTour, ...tourContext };
};
