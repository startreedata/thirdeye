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
import { Box } from "@material-ui/core";
import { some } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Event } from "../../../rest/dto/event.interfaces";
import { SavedStateKeys } from "../../../rest/dto/rca.interfaces";
import { getFromSavedInvestigationOrDefault } from "../../../utils/investigation/investigation.util";
import { serializeKeyValuePair } from "../../../utils/params/params.util";
import { AnomalyFilterOption } from "../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";
import { ChartSection } from "./chart-section/chart-section.component";
import {
    ChartType,
    InvestigationPreviewProps,
    QUERY_PARAM_FOR_CHART_TYPE,
} from "./investigation-preview.interfaces";

export const InvestigationPreview: FunctionComponent<InvestigationPreviewProps> =
    ({
        investigation,
        alertInsight,
        anomaly,
        children,
        title,
        // onInvestigationChange,
    }) => {
        const [searchParams] = useSearchParams();

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

        const [chartType, setChartType] = useState<ChartType>(() => {
            if (searchParams.has(QUERY_PARAM_FOR_CHART_TYPE)) {
                return searchParams.get(
                    QUERY_PARAM_FOR_CHART_TYPE
                ) as ChartType;
            }

            return ChartType.MULTI;
        });

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

        return (
            <Box sx={{ pt: 2 }}>
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
                    setChartType={setChartType}
                    title={title}
                />
                {children}
            </Box>
        );
    };
