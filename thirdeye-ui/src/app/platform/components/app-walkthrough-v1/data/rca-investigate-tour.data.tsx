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

import { Card, CardContent, Chip } from "@material-ui/core";
import { StepType } from "@reactour/tour";
import React from "react";
import { RCA_INVESTIGATE_TOUR_IDS } from "./tour-ids.data";
import {
    CommonStepProps,
    ExtendedStepType,
    getContentElement,
    transformTourSteps,
} from "./tour-utils.data";

export { RCA_INVESTIGATE_TOUR_IDS };
export interface RcaInvestigateStepsProps {
    selectedTab: number;
    handleChangeTab: (v: "heatmap" | "top-contributors" | "events") => void;
}

export const getRcaInvestigateTourSteps = ({
    handleChangeTab,
    setCurrentStep,
}: RcaInvestigateStepsProps & CommonStepProps): ExtendedStepType<
    typeof RCA_INVESTIGATE_TOUR_IDS
>[] =>
    transformTourSteps<typeof RCA_INVESTIGATE_TOUR_IDS>([
        {
            selector: RCA_INVESTIGATE_TOUR_IDS.HEATMAP_BASELINE_OFFSET,
            content: getContentElement({
                title: "Baseline Offset",
                body: (
                    <ul>
                        <li>
                            Baseline Offset lets you view the impact profile of
                            each dimension across time
                        </li>
                        <li>
                            It helps with comparing the anomaly heatmap with
                            various points back in time (4 days ago, 3 weeks
                            ago, 6 months ago, and so on)
                        </li>
                        <li>
                            It is helpful in distinguishing between a sudden
                            spike and a gentle slope on a granular level, which
                            may elude to the factors that caused the observed
                            anomalous behaviour
                        </li>
                    </ul>
                ),
                forceRedraw: true,
                forceRedrawProps: {
                    setCurrentStep,
                    mutationObservable: [
                        RCA_INVESTIGATE_TOUR_IDS.HEATMAP_BASELINE_OFFSET,
                    ],
                },
            }),
            position: "top",
            action: () => {
                handleChangeTab?.("heatmap");
            },
        },
        {
            selector: RCA_INVESTIGATE_TOUR_IDS.HEATMAP_DIMENSION_DRILLS,
            content: getContentElement({
                title: "Dimension Drills",
                body: (
                    <>
                        <p>
                            This is a breakdown of the possible values for each
                            dimension, sized in proportion to prevalence.
                            Hovering over each value displays a tooltip with
                            more information about the absolute value and the
                            percentage change.
                        </p>
                        <Card elevation={0} variant="outlined">
                            <CardContent>
                                Click on a dimensional value to apply it as a
                                filter. The dimension heatmap will refresh
                                according to the selected filter.
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
            selector:
                RCA_INVESTIGATE_TOUR_IDS.HEATMAP_DIMENSION_DRILLS_SELECTED,
            content: getContentElement({
                title: "Dimension Value - Custom Chart Filter",
                body: "Toggling this filter on can enable a better insight into the isolated effect of this value in the recorded data.",
            }),
            action: () => {
                handleChangeTab?.("heatmap");
            },
        },

        {
            selector: RCA_INVESTIGATE_TOUR_IDS.TOP_CONTRIBUTORS,
            content: getContentElement({
                title: "Top Contributors",
                body: (
                    <>
                        This tab shows the list of identified dimension (or a
                        group of dimensions) that have the most contribution to
                        the metrics trend change for a given anomaly, ordered
                        from highest impact to lowest impact.
                    </>
                ),
            }),
            action: () => {
                handleChangeTab?.("top-contributors");
            },
        },
        {
            selector: RCA_INVESTIGATE_TOUR_IDS.EVENTS_INTRO,
            content: getContentElement({
                title: "Events",
                body: (
                    <>
                        <p>
                            Events are specially designated intervals of time
                            where a general abnormal level of activity is
                            expected, like a holiday or a major happening. This
                            tab lets you see if a certain anomaly is correlated
                            with an event, which may help ascertain the root
                            cause for the observed drop/spike.
                        </p>
                        <Card elevation={0} variant="outlined">
                            <CardContent>
                                As an example, if you changed the targeting
                                configuration for a campaign, adding that time
                                period as an event to the investigation might
                                help explain an abnormal frequency of observed
                                anomalies that were unknowingly being caused by
                                that change.
                            </CardContent>
                        </Card>
                        <p>
                            In this example scenario, after reviewing the
                            investigation, restoring that targeting
                            configuration might help reduce/eliminate the
                            observed anomalies.
                        </p>
                    </>
                ),
            }),
            position: "top",
            action: () => {
                handleChangeTab?.("events");
            },
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
                        here. This will log the progress so far as an
                        investigation entry in the anomaly page as shown
                        previously, and be available for anyone to examine in
                        the future
                    </>
                ),
            }),
        },
    ] as StepType[]);
