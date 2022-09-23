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
import { Accordion, Box, Button, Grid, Typography } from "@material-ui/core";
import AccordionDetails from "@material-ui/core/AccordionDetails";
import AccordionSummary from "@material-ui/core/AccordionSummary";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { generateNameForDetectionResult } from "../../../../alert-view/enumeration-items-table/enumeration-items-table.util";
import { generateChartOptionsForAlert } from "../../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeSeriesChart } from "../../../../visualizations/time-series-chart/time-series-chart.component";
import { EvaluationsChartsProps } from "./evaluations-charts.interfaces";

export const EvaluationsCharts: FunctionComponent<EvaluationsChartsProps> = ({
    detectionEvaluations,
    expanded,
    onExpandedChange,
}) => {
    const { t } = useTranslation();

    const handleChange = (detectionName: string, isExpanded: boolean): void => {
        if (isExpanded) {
            onExpandedChange([...expanded, detectionName]);
        } else {
            onExpandedChange(expanded.filter((k) => k !== detectionName));
        }
    };

    const handleExpandAllClick = (): void => {
        onExpandedChange(
            detectionEvaluations.map((evaluation) =>
                generateNameForDetectionResult(evaluation)
            )
        );
    };

    const handleCollapseAllClick = (): void => {
        onExpandedChange([]);
    };

    return (
        <>
            {detectionEvaluations.length > 1 && (
                <Box marginBottom={3}>
                    <Grid container justifyContent="space-between">
                        <Grid item>
                            <Typography variant="h6">
                                {detectionEvaluations.length} enumerations items
                                returned
                            </Typography>
                            <Typography variant="caption">
                                Click name to expand or collapse
                            </Typography>
                        </Grid>
                        <Grid item>
                            {detectionEvaluations.length <= expanded.length && (
                                <Button
                                    variant="text"
                                    onClick={handleCollapseAllClick}
                                >
                                    Collapse all
                                </Button>
                            )}

                            {detectionEvaluations.length > expanded.length && (
                                <Button
                                    variant="text"
                                    onClick={handleExpandAllClick}
                                >
                                    Expand all
                                </Button>
                            )}
                        </Grid>
                    </Grid>
                </Box>
            )}

            {detectionEvaluations.map((detectionEval) => {
                const nameForDetectionAlgorithm =
                    generateNameForDetectionResult(detectionEval);
                const timeseriesConfiguration = generateChartOptionsForAlert(
                    detectionEval,
                    detectionEval.anomalies,
                    t
                );

                timeseriesConfiguration.brush = false;
                timeseriesConfiguration.zoom = true;

                return (
                    <Accordion
                        expanded={expanded.includes(nameForDetectionAlgorithm)}
                        key={nameForDetectionAlgorithm}
                        onChange={(_, isExpanded) =>
                            handleChange(nameForDetectionAlgorithm, isExpanded)
                        }
                    >
                        <AccordionSummary>
                            <ExpandMoreIcon />
                            <Typography>
                                {detectionEval.enumerationItem
                                    ? `Enumeration item: ${generateNameForDetectionResult(
                                          detectionEval
                                      )}`
                                    : ""}
                            </Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                            <TimeSeriesChart
                                height={300}
                                {...timeseriesConfiguration}
                            />
                        </AccordionDetails>
                    </Accordion>
                );
            })}
        </>
    );
};
