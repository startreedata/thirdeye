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
import {
    getContentElement,
    TOURS,
    transformTourSteps,
} from "./tour-types.data";

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

export const getRcaAnomalyTourSteps = transformTourSteps<
    typeof RCA_ANOMALY_TOUR_IDS
>(
    () =>
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
        ] as StepType[]
);
