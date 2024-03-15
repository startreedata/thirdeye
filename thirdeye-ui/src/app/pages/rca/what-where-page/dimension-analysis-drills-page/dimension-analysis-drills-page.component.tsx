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
import { Box, Button, Grid, Typography } from "@material-ui/core";
import { every, isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext } from "react-router-dom";
import { EmptyStateSwitch } from "../../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { AnomalyFilterOption } from "../../../../components/rca/anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import { formatDimensionOptions } from "../../../../components/rca/heat-map/heat-map.utils";
import { PreviewChart } from "../../../../components/rca/top-contributors-table/preview-chart/preview-chart.component";
import {
    AnomalyBreakdown,
    SavedStateKeys,
} from "../../../../rest/dto/rca.interfaces";
import { areFiltersEqual } from "../../../../utils/anomaly-dimension-analysis/anomaly-dimension-analysis";
import { getFromSavedInvestigationOrDefault } from "../../../../utils/investigation/investigation.util";
import { InvestigationContext } from "../../investigation-state-tracker-container-page/investigation-state-tracker.interfaces";
import { DimensionSearchAutocompleteProps } from "../../../../components/rca/heat-map/dimension-search-automcomplete/dimension-search-autocomplete.interfaces";

export interface DimensionAnalysisDrillsPageProps {
    dimensionSearchProps: DimensionSearchAutocompleteProps;
}

export const DimensionAnalysisDrillsPage: FunctionComponent<DimensionAnalysisDrillsPageProps> =
    ({ dimensionSearchProps: { anomalyFilters, heatMapData } }) => {
        const { t } = useTranslation();

        const { investigation, anomaly, alertInsight, onInvestigationChange } =
            useOutletContext<InvestigationContext>();

        const handleAddDimensionsToInvestigationClick = (): void => {
            const currentDimensions = getFromSavedInvestigationOrDefault<
                AnomalyFilterOption[][]
            >(investigation, SavedStateKeys.CHART_FILTER_SET, []);

            const missingDimensionCombinationsFromInvestigation = [
                anomalyFilters,
            ].filter((selected) => {
                return every(
                    currentDimensions.map(
                        (dimensionFilter) =>
                            !areFiltersEqual(dimensionFilter, selected)
                    )
                );
            });

            investigation.uiMetadata[SavedStateKeys.CHART_FILTER_SET] = [
                ...currentDimensions,
                ...missingDimensionCombinationsFromInvestigation,
            ];

            onInvestigationChange(investigation);
        };

        return (
            <Grid container>
                <Grid item xs={12}>
                    <EmptyStateSwitch
                        emptyState={
                            <Box pb={20} pt={20}>
                                <Typography align="center" variant="body1">
                                    {t(
                                        "message.no-data-available-try-a-different-set-of-filters"
                                    )}
                                </Typography>
                            </Box>
                        }
                        isEmpty={
                            !heatMapData ||
                            isEmpty(
                                formatDimensionOptions(
                                    heatMapData as AnomalyBreakdown
                                )
                            )
                        }
                    >
                        <Box pt={2}>
                            <PreviewChart
                                alertInsight={alertInsight}
                                anomaly={anomaly}
                                dimensionCombinations={
                                    isEmpty(anomalyFilters)
                                        ? []
                                        : [anomalyFilters]
                                }
                            >
                                <Button
                                    color="primary"
                                    disabled={isEmpty([anomalyFilters])}
                                    onClick={
                                        handleAddDimensionsToInvestigationClick
                                    }
                                >
                                    {t("label.add-dimensions-to-investigation")}
                                </Button>
                            </PreviewChart>
                        </Box>
                    </EmptyStateSwitch>
                </Grid>
            </Grid>
        );
    };
