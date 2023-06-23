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
import { Grid } from "@material-ui/core";
import { some } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { Event } from "../../../rest/dto/event.interfaces";
import { SavedStateKeys } from "../../../rest/dto/rca.interfaces";
import { getFromSavedInvestigationOrDefault } from "../../../utils/investigation/investigation.util";
import { serializeKeyValuePair } from "../../../utils/params/params.util";
import { AnomalyFilterOption } from "../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";
import { ChartSection } from "./chart-section/chart-section.component";
import { DimensionsSummary } from "./dimensions-summary/dimensions-summary.component";
import { Header } from "./header/header.component";
import {
    ChartType,
    InvestigationPreviewProps,
} from "./investigation-preview.interfaces";

export const InvestigationPreview: FunctionComponent<InvestigationPreviewProps> =
    ({ investigation, alertInsight, anomaly }) => {
        const [isInitialSetup, setIsInitialSetup] = useState(true);

        // useEffect will set this to the appropriate state
        const [dimensionCombinationsToChart, setDimensionCombinationsToChart] =
            useState<AnomalyFilterOption[][]>([]);

        // useEffect will set this to the appropriate state
        const [events, setEvents] = useState<Event[]>([]);

        const [
            selectedDimensionCombinations,
            setSelectedDimensionCombinations,
        ] = useState<Set<string>>(new Set());

        const [chartType, setChartType] = useState<ChartType>(ChartType.ONE);

        useEffect(() => {
            // #TODO only replace if actual contents change
            if (investigation) {
                const allDimensionCombinations =
                    getFromSavedInvestigationOrDefault<AnomalyFilterOption[][]>(
                        investigation,
                        SavedStateKeys.CHART_FILTER_SET,
                        []
                    );

                setDimensionCombinationsToChart((localCopy) => {
                    const toAdd = allDimensionCombinations.filter(
                        (dimensionComboInInvestigation) => {
                            const serialized = serializeKeyValuePair(
                                dimensionComboInInvestigation
                            );

                            return !some(
                                localCopy.map(
                                    (dimensionCombo) =>
                                        serializeKeyValuePair(
                                            dimensionCombo
                                        ) === serialized
                                )
                            );
                        }
                    );

                    if (!isInitialSetup) {
                        setSelectedDimensionCombinations(
                            new Set([
                                ...selectedDimensionCombinations,
                                ...toAdd.map((d) => serializeKeyValuePair(d)),
                            ])
                        );
                    }

                    return [...localCopy, ...toAdd];
                });

                if (isInitialSetup) {
                    setSelectedDimensionCombinations(
                        new Set(
                            allDimensionCombinations.map((d) =>
                                serializeKeyValuePair(d)
                            )
                        )
                    );
                    setIsInitialSetup(false);
                }

                setEvents(
                    getFromSavedInvestigationOrDefault<Event[]>(
                        investigation,
                        SavedStateKeys.EVENT_SET,
                        []
                    )
                );
            }
        }, [investigation]);

        const handleCheckBoxClick = (
            dimensionCombination: AnomalyFilterOption[],
            shouldAdd: boolean
        ): void => {
            const serializedStr = serializeKeyValuePair(dimensionCombination);

            setSelectedDimensionCombinations((current) => {
                if (shouldAdd) {
                    current.add(serializedStr);
                } else {
                    current.delete(serializedStr);
                }

                return new Set(current);
            });
        };

        const handleDeleteClick = (
            dimensionCombination: AnomalyFilterOption[]
        ): void => {
            const serializedStr = serializeKeyValuePair(dimensionCombination);

            setDimensionCombinationsToChart(
                dimensionCombinationsToChart.filter(
                    (existingDimensionCombination) => {
                        return (
                            serializeKeyValuePair(
                                existingDimensionCombination
                            ) !== serializedStr
                        );
                    }
                )
            );
            setSelectedDimensionCombinations((current) => {
                current.delete(serializedStr);

                return new Set(current);
            });
        };

        return (
            <Grid container>
                <Grid item xs={12}>
                    <Header
                        selectedChartType={chartType}
                        onOptionClick={setChartType}
                    />
                </Grid>
                <Grid item xs={12}>
                    <Grid container>
                        <Grid item lg={4} md={4} sm={5} xs={12}>
                            <DimensionsSummary
                                availableDimensionCombinations={
                                    dimensionCombinationsToChart
                                }
                                selectedDimensionCombinations={
                                    selectedDimensionCombinations
                                }
                                onCheckBoxClick={handleCheckBoxClick}
                                onDeleteClick={handleDeleteClick}
                            />
                        </Grid>
                        <Grid item lg={8} md={8} sm={7} xs={12}>
                            <ChartSection
                                alertInsight={alertInsight}
                                anomaly={anomaly}
                                availableDimensionCombinations={
                                    dimensionCombinationsToChart
                                }
                                chartType={chartType}
                                events={events}
                                selectedDimensionCombinations={
                                    selectedDimensionCombinations
                                }
                            />
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        );
    };
