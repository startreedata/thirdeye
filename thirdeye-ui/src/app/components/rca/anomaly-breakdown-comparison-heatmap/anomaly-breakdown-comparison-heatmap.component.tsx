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
import {
    Box,
    Button,
    CardContent,
    Chip,
    Divider,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { HierarchyNode } from "d3-hierarchy";
import { isEmpty, isString, pull } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { useNotificationProviderV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAnomalyMetricBreakdown } from "../../../rest/rca/rca.actions";
import { EMPTY_STRING_DISPLAY } from "../../../utils/anomalies/anomalies.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import {
    concatKeyValueWithEqual,
    deserializeKeyValuePair,
    serializeKeyValuePair,
} from "../../../utils/params/params.util";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { Treemap } from "../../visualizations/treemap/treemap.component";
import { TreemapData } from "../../visualizations/treemap/treemap.interfaces";
import {
    AnomalyBreakdownComparisonData,
    AnomalyBreakdownComparisonDataByDimensionColumn,
    AnomalyBreakdownComparisonHeatmapProps,
    AnomalyFilterOption,
    DimensionDisplayData,
} from "./anomaly-breakdown-comparison-heatmap.interfaces";
import { useAnomalyBreakdownComparisonHeatmapStyles } from "./anomaly-breakdown-comparison-heatmap.styles";
import {
    formatComparisonData,
    formatDimensionOptions,
    formatTreemapData,
} from "./anomaly-breakdown-comparison-heatmap.utils";
import { DimensionHeatmapTooltip } from "./dimension-heatmap-tooltip/dimension-heatmap-tooltip.component";

const HEATMAP_FILTERS_URL_KEY = "heatmapFilters";

export const AnomalyBreakdownComparisonHeatmap: FunctionComponent<AnomalyBreakdownComparisonHeatmapProps> =
    ({
        anomalyId,
        shouldTruncateText = true,
        comparisonOffset,
        onAddFilterSetClick,
        chartTimeSeriesFilterSet,
    }) => {
        const classes = useAnomalyBreakdownComparisonHeatmapStyles();
        const { t } = useTranslation();
        const {
            anomalyMetricBreakdown,
            getMetricBreakdown,
            status: anomalyBreakdownReqStatus,
            errorMessages: anomalyBreakdownReqErrors,
        } = useGetAnomalyMetricBreakdown();
        const [shouldDisplayRemoveText, setShouldDisplayRemoveText] =
            useState<boolean>(false);
        const [breakdownComparisonData, setBreakdownComparisonData] = useState<
            AnomalyBreakdownComparisonDataByDimensionColumn[] | null
        >(null);
        const [anomalyFilterOptions, setAnomalyFilterOptions] = useState<
            AnomalyFilterOption[]
        >([]);
        const [searchParams, setSearchParams] = useSearchParams();
        const { notify } = useNotificationProviderV1();

        const heatmapFilterQueryParams = searchParams.get(
            HEATMAP_FILTERS_URL_KEY
        );
        const [anomalyFilters, setAnomalyFilters] = useState<
            AnomalyFilterOption[]
        >(
            heatmapFilterQueryParams
                ? deserializeKeyValuePair(heatmapFilterQueryParams)
                : []
        );

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
            if (!anomalyMetricBreakdown) {
                setBreakdownComparisonData(null);

                return;
            }

            if (anomalyFilterOptions.length === 0) {
                setAnomalyFilterOptions(
                    formatDimensionOptions(anomalyMetricBreakdown)
                );
            }

            setBreakdownComparisonData(
                formatComparisonData(anomalyMetricBreakdown)
            );
        }, [anomalyMetricBreakdown]);

        /**
         * This is often triggered multiple times within a re-render
         */
        useEffect(() => {
            getMetricBreakdown(anomalyId, {
                baselineOffset: comparisonOffset,
                filters: [
                    ...anomalyFilters.map((item) =>
                        concatKeyValueWithEqual(item, false)
                    ),
                ],
            });
        }, [anomalyId, comparisonOffset, anomalyFilters]);

        useEffect(() => {
            const id = anomalyFilters
                .map((item) => concatKeyValueWithEqual(item, false))
                .sort()
                .join();
            const existsInSet = chartTimeSeriesFilterSet.some(
                (filterSet) =>
                    filterSet
                        .map((item) => concatKeyValueWithEqual(item, false))
                        .sort()
                        .join() === id
            );
            setShouldDisplayRemoveText(existsInSet);
        }, [chartTimeSeriesFilterSet, anomalyFilters]);

        const handleNodeClick = (
            tileData: HierarchyNode<
                TreemapData<AnomalyBreakdownComparisonData>
            >,
            dimensionColumn: string
        ): void => {
            if (!tileData) {
                return;
            }

            const resultantFilters: AnomalyFilterOption[] = [
                ...anomalyFilters,
                {
                    key: dimensionColumn,
                    value: tileData.data.id,
                },
            ];
            handleFilterChange([...resultantFilters]);
        };

        const handleNodeFilterOnDelete = (node: AnomalyFilterOption): void => {
            const resultantFilters = pull(anomalyFilters, node);
            handleFilterChange([...resultantFilters]);
        };

        const handleOnChangeFilter = (options: AnomalyFilterOption[]): void => {
            handleFilterChange([...options]);
        };

        const handleFilterChange = (
            newFilters: AnomalyFilterOption[]
        ): void => {
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

        const colorChangeValueAccessor = (
            node: TreemapData<AnomalyBreakdownComparisonData>
        ): number => {
            if (node.extraData) {
                return node.extraData.contributionDiff * 100;
            }

            return node.size;
        };

        useEffect(() => {
            notifyIfErrors(
                anomalyBreakdownReqStatus,
                anomalyBreakdownReqErrors,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.heatmap-data"),
                })
            );
        }, [anomalyBreakdownReqStatus, anomalyBreakdownReqErrors]);

        return (
            <>
                <CardContent>
                    {anomalyFilterOptions && (
                        <Grid container className={classes.filtersContainer}>
                            <Grid item xs={12}>
                                <Box mt={2}>
                                    <Typography variant="h6">
                                        Filter Data Controls
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item md={10} sm={9} xs={12}>
                                <Autocomplete
                                    freeSolo
                                    multiple
                                    getOptionLabel={(
                                        option: AnomalyFilterOption
                                    ) =>
                                        isString(option.value)
                                            ? option.value ||
                                              EMPTY_STRING_DISPLAY
                                            : ""
                                    }
                                    groupBy={(option: AnomalyFilterOption) =>
                                        isString(option.key) ? option.key : ""
                                    }
                                    options={anomalyFilterOptions || []}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            fullWidth
                                            placeholder={t(
                                                "message.anomaly-filter-search"
                                            )}
                                            variant="outlined"
                                        />
                                    )}
                                    renderTags={() =>
                                        anomalyFilters.map(
                                            (
                                                option: AnomalyFilterOption,
                                                index
                                            ) => (
                                                <Chip
                                                    className="filter-chip"
                                                    key={`${index}_${option.value}`}
                                                    label={`${option.key}=${
                                                        option.value ||
                                                        EMPTY_STRING_DISPLAY
                                                    }`}
                                                    size="small"
                                                    style={{
                                                        marginTop: 0,
                                                        marginBottom: 0,
                                                        marginRight: 3,
                                                    }}
                                                    onDelete={() =>
                                                        handleNodeFilterOnDelete(
                                                            option
                                                        )
                                                    }
                                                />
                                            )
                                        )
                                    }
                                    value={anomalyFilters}
                                    onChange={(_e, options) =>
                                        handleOnChangeFilter(
                                            options as AnomalyFilterOption[]
                                        )
                                    }
                                />
                            </Grid>
                            <Grid item md={2} sm={3} xs={12}>
                                <Button
                                    fullWidth
                                    color="primary"
                                    disabled={anomalyFilters.length === 0}
                                    style={{ height: "100%" }}
                                    variant={
                                        shouldDisplayRemoveText
                                            ? "outlined"
                                            : "contained"
                                    }
                                    onClick={() =>
                                        anomalyFilters.length > 0 &&
                                        onAddFilterSetClick &&
                                        onAddFilterSetClick(anomalyFilters)
                                    }
                                >
                                    {shouldDisplayRemoveText
                                        ? t("label.remove-from-chart")
                                        : t("label.add-to-chart")}
                                </Button>
                            </Grid>
                        </Grid>
                    )}
                </CardContent>
                <CardContent>
                    <LoadingErrorStateSwitch
                        isError={
                            anomalyBreakdownReqStatus === ActionStatus.Error
                        }
                        isLoading={
                            anomalyBreakdownReqStatus ===
                                ActionStatus.Working ||
                            anomalyBreakdownReqStatus === ActionStatus.Initial
                        }
                    >
                        <EmptyStateSwitch
                            emptyState={
                                <Box pb={20} pt={20}>
                                    <Typography align="center" variant="body1">
                                        {t("message.no-data")}
                                    </Typography>
                                </Box>
                            }
                            isEmpty={isEmpty(breakdownComparisonData)}
                        >
                            {React.Children.toArray(
                                breakdownComparisonData &&
                                    breakdownComparisonData.map((data) => (
                                        <>
                                            <Divider />
                                            <Treemap<
                                                AnomalyBreakdownComparisonData &
                                                    DimensionDisplayData
                                            >
                                                colorChangeValueAccessor={
                                                    colorChangeValueAccessor
                                                }
                                                name={data.column}
                                                shouldTruncateText={
                                                    shouldTruncateText
                                                }
                                                tooltipElement={
                                                    DimensionHeatmapTooltip
                                                }
                                                treemapData={formatTreemapData(
                                                    data,
                                                    data.column
                                                )}
                                                onDimensionClickHandler={(
                                                    node
                                                ) =>
                                                    handleNodeClick(
                                                        node,
                                                        data.column
                                                    )
                                                }
                                            />
                                        </>
                                    ))
                            )}
                        </EmptyStateSwitch>
                    </LoadingErrorStateSwitch>
                </CardContent>
            </>
        );
    };
