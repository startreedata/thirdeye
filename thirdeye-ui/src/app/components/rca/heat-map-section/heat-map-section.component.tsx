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
import Typography from "@material-ui/core/Typography";
import every from "lodash/every";
import isEmpty from "lodash/isEmpty";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useParams, useSearchParams } from "react-router-dom";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomalyFilterOption } from "../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import { DimensionSearchAutocomplete } from "../heat-map/dimension-search-automcomplete/dimension-search-autocomplete.component";
import { HeatMap } from "../heat-map/heat-map.component";
import { formatDimensionOptions } from "../heat-map/heat-map.utils";
import { PreviewChart } from "../top-contributors-table/preview-chart/preview-chart.component";
import {
    PageContentsCardV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    AnomalyBreakdown,
    SavedStateKeys,
} from "../../../rest/dto/rca.interfaces";
import { useGetAnomalyMetricBreakdown } from "../../../rest/rca/rca.actions";
import { areFiltersEqual } from "../../../utils/anomaly-dimension-analysis/anomaly-dimension-analysis";
import { getFromSavedInvestigationOrDefault } from "../../../utils/investigation/investigation.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import {
    concatKeyValueWithEqual,
    deserializeKeyValuePair,
    serializeKeyValuePair,
} from "../../../utils/params/params.util";
import { RootCauseAnalysisForAnomalyPageParams } from "../../../pages/root-cause-analysis-for-anomaly-page/root-cause-analysis-for-anomaly-page.interfaces";
import { InvestigationContext } from "../../../pages/rca/investigation-state-tracker-container-page/investigation-state-tracker.interfaces";
import { HeatMapSectionProps } from "./heat-map-section.interfaces";

const HEATMAP_FILTERS_URL_KEY = "heatmapFilters";

export const HeatMapPage: FunctionComponent<HeatMapSectionProps> = ({
    comparisonOffset,
    dimensionsInOrder,
}) => {
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const { id: anomalyId } =
        useParams<RootCauseAnalysisForAnomalyPageParams>();

    // Keep track of last height to use as loading state height
    const [lastTreemapHeight, setLastTreemapHeight] = useState(300);

    const [anomalyFilters, setAnomalyFilters] = useState<AnomalyFilterOption[]>(
        () => {
            const heatmapFilterQueryParams = searchParams.get(
                HEATMAP_FILTERS_URL_KEY
            );

            return heatmapFilterQueryParams
                ? deserializeKeyValuePair(heatmapFilterQueryParams)
                : [];
        }
    );

    const { investigation, anomaly, alertInsight, onInvestigationChange } =
        useOutletContext<InvestigationContext>();

    const {
        anomalyMetricBreakdown: heatMapData,
        getMetricBreakdown,
        status: heatMapDataRequestStatus,
        errorMessages,
    } = useGetAnomalyMetricBreakdown();

    // Sync the anomaly filters if the search params changed
    useEffect(() => {
        const currentQueryFilterSearchQuery = searchParams.get(
            HEATMAP_FILTERS_URL_KEY
        );

        if (
            currentQueryFilterSearchQuery &&
            currentQueryFilterSearchQuery !==
                serializeKeyValuePair(anomalyFilters)
        ) {
            setAnomalyFilters(
                deserializeKeyValuePair(currentQueryFilterSearchQuery)
            );
        } else if (
            currentQueryFilterSearchQuery === null &&
            anomalyFilters.length !== 0 // check for 0 so we don't trigger necessary change
        ) {
            setAnomalyFilters([]);
        }
    }, [searchParams]);

    useEffect(() => {
        getMetricBreakdown(Number(anomalyId), {
            baselineOffset: comparisonOffset,
            filters: [
                ...anomalyFilters.map((item) =>
                    concatKeyValueWithEqual(item, false)
                ),
            ],
        });
    }, [anomalyId, comparisonOffset, anomalyFilters]);

    useEffect(() => {
        notifyIfErrors(
            heatMapDataRequestStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: "Heat map data",
            })
        );
    }, [heatMapDataRequestStatus]);

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

    const handleFilterChange = (newFilters: AnomalyFilterOption[]): void => {
        if (newFilters.length === 0) {
            searchParams.delete(HEATMAP_FILTERS_URL_KEY);
        } else {
            searchParams.set(
                HEATMAP_FILTERS_URL_KEY,
                serializeKeyValuePair(newFilters)
            );
        }
        setSearchParams(searchParams);
        setAnomalyFilters(newFilters);
    };

    return (
        <PageContentsCardV1>
            <Grid container>
                <Grid item xs={12}>
                    <Typography variant="h5">
                        {t("label.heatmap-and-dimension-drills")}
                    </Typography>
                    <Typography variant="body2">
                        {t(
                            "message.represents-the-frequency-and-severity-of-a-change"
                        )}
                    </Typography>
                </Grid>

                <Grid item xs={12}>
                    <DimensionSearchAutocomplete
                        anomalyFilters={anomalyFilters}
                        heatMapData={heatMapData as AnomalyBreakdown}
                        onFilterChange={handleFilterChange}
                    />
                    <LoadingErrorStateSwitch
                        isError={
                            heatMapDataRequestStatus === ActionStatus.Error
                        }
                        isLoading={
                            heatMapDataRequestStatus === ActionStatus.Initial ||
                            heatMapDataRequestStatus === ActionStatus.Working
                        }
                        loadingState={
                            <Box pb={2} pt={2}>
                                <SkeletonV1
                                    animation="pulse"
                                    height={lastTreemapHeight}
                                    variant="rect"
                                />
                            </Box>
                        }
                    >
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
                            <HeatMap
                                anomalyFilters={anomalyFilters}
                                dimensionsInOrder={dimensionsInOrder}
                                heatMapData={heatMapData as AnomalyBreakdown}
                                onFilterChange={handleFilterChange}
                                onHeightChange={setLastTreemapHeight}
                            />
                        </EmptyStateSwitch>

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
                    </LoadingErrorStateSwitch>
                </Grid>
            </Grid>
        </PageContentsCardV1>
    );
};
