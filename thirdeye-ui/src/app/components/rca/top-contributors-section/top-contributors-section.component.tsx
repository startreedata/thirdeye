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
import Box from "@material-ui/core/Box";
import Button from "@material-ui/core/Button";
import Grid from "@material-ui/core/Grid";
import every from "lodash/every";
import isEmpty from "lodash/isEmpty";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useParams } from "react-router-dom";
import { InvestigationContext } from "../../../pages/rca/investigation-state-tracker-container-page/investigation-state-tracker.interfaces";
import { RootCauseAnalysisForAnomalyPageParams } from "../../../pages/root-cause-analysis-for-anomaly-page/root-cause-analysis-for-anomaly-page.interfaces";
import {
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    AnomalyDimensionAnalysisData,
    SavedStateKeys,
} from "../../../rest/dto/rca.interfaces";
import { areFiltersEqual } from "../../../utils/anomaly-dimension-analysis/anomaly-dimension-analysis";
import { getFromSavedInvestigationOrDefault } from "../../../utils/investigation/investigation.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { serializeKeyValuePair } from "../../../utils/params/params.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomalyFilterOption } from "../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import { PreviewChart } from "../top-contributors-table/preview-chart/preview-chart.component";
import { TopContributorsTable } from "../top-contributors-table/top-contributors-table.component";
import { TopContributorsSectionProps } from "./top-contributors-section.interfaces";

export const TopContributorsSection: FunctionComponent<TopContributorsSectionProps> =
    ({ comparisonOffset, anomalyDimensionAnalysisFetch }) => {
        const { notify } = useNotificationProviderV1();
        const { t } = useTranslation();
        const { id: anomalyId } =
            useParams<RootCauseAnalysisForAnomalyPageParams>();
        const { investigation, anomaly, alertInsight, onInvestigationChange } =
            useOutletContext<InvestigationContext>();

        const {
            anomalyDimensionAnalysisData,
            getDimensionAnalysisData,
            status: anomalyDimensionAnalysisReqStatus,
            errorMessages,
        } = anomalyDimensionAnalysisFetch;

        const [chartTimeSeriesFilterSet, setChartTimeSeriesFilterSet] =
            useState<AnomalyFilterOption[][]>(
                getFromSavedInvestigationOrDefault<AnomalyFilterOption[][]>(
                    investigation,
                    SavedStateKeys.CHART_FILTER_SET,
                    []
                )
            );

        useEffect(() => {
            getDimensionAnalysisData(Number(anomalyId), {
                baselineOffset: comparisonOffset,
            });
        }, [anomalyId, comparisonOffset]);

        useEffect(() => {
            notifyIfErrors(
                anomalyDimensionAnalysisReqStatus,
                errorMessages,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.dimension-analysis-data"),
                })
            );
        }, [anomalyDimensionAnalysisReqStatus]);

        const handleRemoveBtnClick = (idx: number): void => {
            setChartTimeSeriesFilterSet((original) =>
                original.filter((_, index) => index !== idx)
            );
        };

        const handleDimensionCombinationClick = (
            filters: AnomalyFilterOption[]
        ): void => {
            const serializedFilters = serializeKeyValuePair(filters);
            const existingIndex = chartTimeSeriesFilterSet.findIndex(
                (existingFilters) =>
                    serializeKeyValuePair(existingFilters) === serializedFilters
            );
            if (existingIndex === -1) {
                setChartTimeSeriesFilterSet((original) => [
                    ...original,
                    [...filters], // Make a copy of filters so changes to the reference one doesn't affect it
                ]);
            } else {
                handleRemoveBtnClick(existingIndex);
            }
        };

        const handleAddDimensionsToInvestigationClick = (): void => {
            const currentDimensions = getFromSavedInvestigationOrDefault<
                AnomalyFilterOption[][]
            >(investigation, SavedStateKeys.CHART_FILTER_SET, []);

            const missingDimensionCombinationsFromInvestigation =
                chartTimeSeriesFilterSet.filter((selected) => {
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
                    <LoadingErrorStateSwitch
                        isError={
                            anomalyDimensionAnalysisReqStatus ===
                            ActionStatus.Error
                        }
                        isLoading={
                            anomalyDimensionAnalysisReqStatus ===
                                ActionStatus.Initial ||
                            anomalyDimensionAnalysisReqStatus ===
                                ActionStatus.Working
                        }
                        loadingState={
                            <Box pb={2} pt={2}>
                                <SkeletonV1
                                    animation="pulse"
                                    height={300}
                                    variant="rect"
                                />
                            </Box>
                        }
                    >
                        <EmptyStateSwitch
                            emptyState={
                                <Box pb={20} pt={20}>
                                    <NoDataIndicator
                                        text={
                                            anomalyDimensionAnalysisData
                                                ?.analysisRunInfo?.message || ""
                                        }
                                    />
                                </Box>
                            }
                            isEmpty={
                                !anomalyDimensionAnalysisData?.analysisRunInfo
                                    ?.success
                            }
                        >
                            <TopContributorsTable
                                alertInsight={alertInsight}
                                anomaly={anomaly}
                                anomalyDimensionAnalysisData={
                                    anomalyDimensionAnalysisData as AnomalyDimensionAnalysisData
                                }
                                chartTimeSeriesFilterSet={
                                    chartTimeSeriesFilterSet
                                }
                                comparisonOffset={comparisonOffset}
                                onCheckClick={handleDimensionCombinationClick}
                            />
                            <Box pt={2}>
                                <PreviewChart
                                    alertInsight={alertInsight}
                                    anomaly={anomaly}
                                    dimensionCombinations={
                                        chartTimeSeriesFilterSet
                                    }
                                >
                                    <Button
                                        color="primary"
                                        disabled={isEmpty(
                                            chartTimeSeriesFilterSet
                                        )}
                                        onClick={
                                            handleAddDimensionsToInvestigationClick
                                        }
                                    >
                                        {t(
                                            "label.add-dimensions-to-investigation"
                                        )}
                                    </Button>
                                </PreviewChart>
                            </Box>
                        </EmptyStateSwitch>
                    </LoadingErrorStateSwitch>
                </Grid>
            </Grid>
        );
    };
