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
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAnomalyMetricBreakdown } from "../../rest/rca/rca.actions";
import { EMPTY_STRING_DISPLAY } from "../../utils/anomalies/anomalies.util";
import {
    concatKeyValueWithEqual,
    deserializeKeyValuePair,
    serializeKeyValuePair,
} from "../../utils/params/params.util";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { Treemap } from "../visualizations/treemap/treemap.component";
import { TreemapData } from "../visualizations/treemap/treemap.interfaces";
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

export const AnomalyBreakdownComparisonHeatmap: FunctionComponent<
    AnomalyBreakdownComparisonHeatmapProps
> = ({
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

    const heatmapFilterQueryParams = searchParams.get(HEATMAP_FILTERS_URL_KEY);
    const [anomalyFilters, setAnomalyFilters] = useState<AnomalyFilterOption[]>(
        heatmapFilterQueryParams
            ? deserializeKeyValuePair(heatmapFilterQueryParams)
            : []
    );

    // Sync the anomaly filters if it the search params changed
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
        } else if (currentQueryFilterSearchQuery === null) {
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
            filters: [...anomalyFilters.map(concatKeyValueWithEqual)],
        });
    }, [anomalyId, comparisonOffset, anomalyFilters]);

    useEffect(() => {
        const id = anomalyFilters.map(concatKeyValueWithEqual).sort().join();
        const existsInSet = chartTimeSeriesFilterSet.some(
            (filterSet) =>
                filterSet.map(concatKeyValueWithEqual).sort().join() === id
        );
        setShouldDisplayRemoveText(existsInSet);
    }, [chartTimeSeriesFilterSet, anomalyFilters]);

    const handleNodeClick = (
        tileData: HierarchyNode<TreemapData<AnomalyBreakdownComparisonData>>,
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

    const colorChangeValueAccessor = (
        node: TreemapData<AnomalyBreakdownComparisonData>
    ): number => {
        if (node.extraData) {
            return node.extraData.contributionDiff * 100;
        }

        return node.size;
    };

    useEffect(() => {
        if (anomalyBreakdownReqStatus === ActionStatus.Error) {
            !isEmpty(anomalyBreakdownReqErrors)
                ? anomalyBreakdownReqErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.heatmap-data"),
                      })
                  );
        }
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
                                getOptionLabel={(option: AnomalyFilterOption) =>
                                    isString(option.value)
                                        ? option.value || EMPTY_STRING_DISPLAY
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
                {/* Loading Indicator when requests are in flight */}
                {anomalyBreakdownReqStatus === ActionStatus.Working && (
                    <Box pb={20} pt={20}>
                        <AppLoadingIndicatorV1 />
                    </Box>
                )}

                {/* If breakdownComparisonData is not empty render treemaps */}
                {anomalyBreakdownReqStatus === ActionStatus.Done &&
                    !isEmpty(breakdownComparisonData) &&
                    React.Children.toArray(
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
                                        shouldTruncateText={shouldTruncateText}
                                        tooltipElement={DimensionHeatmapTooltip}
                                        treemapData={formatTreemapData(
                                            data,
                                            data.column
                                        )}
                                        onDimensionClickHandler={(node) =>
                                            handleNodeClick(node, data.column)
                                        }
                                    />
                                </>
                            ))
                    )}

                {/* Indicate no data if breakdown data is missing and requests are complete */}
                {anomalyBreakdownReqStatus === ActionStatus.Done &&
                    isEmpty(breakdownComparisonData) && (
                        <Grid item xs={12}>
                            <Box pb={20} pt={20}>
                                <Typography align="center" variant="body1">
                                    {t("message.no-data")}
                                </Typography>
                            </Box>
                        </Grid>
                    )}

                {/* Indicate no data if there was an error */}
                {anomalyBreakdownReqStatus === ActionStatus.Error && (
                    <Grid item xs={12}>
                        <Box pb={20} pt={20}>
                            <NoDataIndicator />
                        </Box>
                    </Grid>
                )}
            </CardContent>
        </>
    );
};
