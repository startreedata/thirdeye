import { Chip, Divider, Grid, TextField, Typography } from "@material-ui/core";
import { Autocomplete, createFilterOptions } from "@material-ui/lab";
import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { isEmpty, isString, map, pull } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { AnomalyBreakdown } from "../../rest/dto/rca.interfaces";
import { getAnomalyMetricBreakdown } from "../../rest/rca/rca.rest";
import { Treemap } from "../visualizations/treemap/treemap.component";
import { TreemapData } from "../visualizations/treemap/treemap.interfaces";
import {
    AnomalyBreakdownComparisonData,
    AnomalyBreakdownComparisonDataByDimensionColumn,
    AnomalyBreakdownComparisonHeatmapProps,
    AnomalyFilterOptions,
    SummarizeDataFunctionParams,
    SummaryData,
} from "./anomaly-breakdown-comparison-heatmap.interfaces";
import { useAnomalyBreakdownComparisonHeatmapStyles } from "./anomaly-breakdown-comparison-heatmap.styles";
import { DimensionHeatmapTooltip } from "./dimension-heatmap-tooltip/dimension-heatmap-tooltip.component";

const OPTIONS_LIMIT = 10;
const filterOptions = createFilterOptions({
    limit: OPTIONS_LIMIT,
});

function summarizeDimensionValueData(
    dimensionValueData: SummarizeDataFunctionParams
): SummaryData {
    const summarized: SummaryData = {};
    if (!dimensionValueData) {
        return summarized;
    }

    const totalCount = Object.keys(dimensionValueData).reduce(
        (total, dimensionValueKey) =>
            total + dimensionValueData[dimensionValueKey],
        0
    );

    Object.keys(dimensionValueData).forEach((dimension: string) => {
        summarized[dimension] = {
            count: dimensionValueData[dimension],
            percentage: dimensionValueData[dimension] / totalCount,
        };
    });

    return summarized;
}

function formatTreemapData(
    dimensionData: AnomalyBreakdownComparisonDataByDimensionColumn
): TreemapData<AnomalyBreakdownComparisonData>[] {
    return [
        { id: dimensionData.column, size: 0, parent: null },
        ...map(dimensionData.dimensionComparisonData, (comparisonData, k) => {
            return {
                id: k,
                size: comparisonData.current,
                parent: dimensionData.column,
                extraData: comparisonData,
            };
        }),
    ];
}

export const AnomalyBreakdownComparisonHeatmap: FunctionComponent<AnomalyBreakdownComparisonHeatmapProps> = ({
    anomalyId,
    comparisonOffset = AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO,
}: AnomalyBreakdownComparisonHeatmapProps) => {
    const classes = useAnomalyBreakdownComparisonHeatmapStyles();
    const { t } = useTranslation();
    const [
        anomalyBreakdownCurrent,
        setAnomalyBreakdownCurrent,
    ] = useState<AnomalyBreakdown | null>(null);
    const [
        anomalyBreakdownComparison,
        setAnomalyBreakdownComparison,
    ] = useState<AnomalyBreakdown | null>(null);
    const [breakdownComparisonData, setBreakdownComparisonData] = useState<
        AnomalyBreakdownComparisonDataByDimensionColumn[] | null
    >(null);
    const [anomalyFilters, setAnomalyFilters] = useState<
        AnomalyFilterOptions[]
    >([]);
    const [anomalyFilterOptions, setAnomalyFilterOptions] = useState<
        AnomalyFilterOptions[]
    >([]);
    const [loading, setLoading] = useState<boolean>(false);

    useEffect(() => {
        setLoading(true);
        if (!anomalyBreakdownCurrent || !anomalyBreakdownComparison) {
            setBreakdownComparisonData(null);

            return;
        }

        const breakdownComparisonDataByDimensionColumn: AnomalyBreakdownComparisonDataByDimensionColumn[] = [];

        if (!anomalyFilterOptions.length) {
            let optionsMenu: AnomalyFilterOptions[] = [];
            Object.keys(anomalyBreakdownCurrent).forEach(
                (dimensionColumnName) => {
                    const options = Object.keys(
                        anomalyBreakdownCurrent[dimensionColumnName]
                    ).map((value) => ({
                        key: dimensionColumnName,
                        value,
                    }));
                    optionsMenu = [...optionsMenu, ...options];
                }
            );
            setAnomalyFilterOptions(optionsMenu);
        }

        Object.keys(anomalyBreakdownCurrent).forEach((dimensionColumnName) => {
            const currentDimensionValuesData = summarizeDimensionValueData(
                anomalyBreakdownCurrent[dimensionColumnName]
            );
            const comparisonDimensionValuesData = summarizeDimensionValueData(
                anomalyBreakdownComparison[dimensionColumnName]
            );
            const dimensionComparisonData: {
                [key: string]: AnomalyBreakdownComparisonData;
            } = {};

            Object.keys(currentDimensionValuesData).forEach(
                (dimension: string) => {
                    dimensionComparisonData[dimension] = {
                        current: currentDimensionValuesData[dimension].count,
                        currentPercentage:
                            currentDimensionValuesData[dimension].percentage,
                        comparison:
                            comparisonDimensionValuesData[dimension]?.count ||
                            0,
                        comparisonPercentage:
                            comparisonDimensionValuesData[dimension]
                                ?.percentage,
                        percentageDiff:
                            (currentDimensionValuesData[dimension]
                                ?.percentage || 0) -
                            (comparisonDimensionValuesData[dimension]
                                ?.percentage || 0),
                    };
                }
            );

            breakdownComparisonDataByDimensionColumn.push({
                column: dimensionColumnName,
                dimensionComparisonData,
            });
        });
        setBreakdownComparisonData(breakdownComparisonDataByDimensionColumn);
        setLoading(false);
    }, [anomalyBreakdownCurrent, anomalyBreakdownComparison]);

    useEffect(() => {
        setLoading(true);
        setAnomalyBreakdownCurrent(null);
        setAnomalyBreakdownComparison(null);

        getAnomalyMetricBreakdown(anomalyId, {
            timezone: "UTC",
            filters: [
                ...anomalyFilters.map(
                    (option) => `${option.key}=${option.value}`
                ),
            ],
        }).then((anomalyBreakdown) => {
            setAnomalyBreakdownCurrent(anomalyBreakdown);
        });

        getAnomalyMetricBreakdown(anomalyId, {
            timezone: "UTC",
            offset: comparisonOffset,
            filters: [
                ...anomalyFilters.map(
                    (option) => `${option.key}=${option.value}`
                ),
            ],
        }).then((anomalyBreakdown) => {
            setAnomalyBreakdownComparison(anomalyBreakdown);
        });
    }, [anomalyId, comparisonOffset, anomalyFilters]);

    const handleNodeClick = (node: AnomalyFilterOptions): void => {
        if (!node) {
            return;
        }

        setLoading(true);
        let resultantFilters: AnomalyFilterOptions[] = [];

        if (anomalyFilters.includes(node)) {
            resultantFilters = pull(anomalyFilters, node);
        } else {
            resultantFilters = [...anomalyFilters, node];
        }
        setAnomalyFilters([...resultantFilters]);
        setLoading(false);
    };

    const handleOnChangeFilter = (options: AnomalyFilterOptions[]): void => {
        setAnomalyFilters([...options]);
    };

    return (
        <>
            <Grid container className={classes.filtersContainer} xs={12}>
                {anomalyFilterOptions && (
                    <Grid item xs={6}>
                        <Autocomplete
                            freeSolo
                            multiple
                            filterOptions={filterOptions}
                            getOptionLabel={(option: any) =>
                                isString(option.value) ? option.value : ""
                            }
                            groupBy={(option: any) =>
                                isString(option.key) ? option.key : ""
                            }
                            options={anomalyFilterOptions || []}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    fullWidth
                                    label={t("label.filter-by")}
                                    placeholder={t(
                                        "message.anomaly-filter-search"
                                    )}
                                    variant="outlined"
                                />
                            )}
                            renderTags={() =>
                                anomalyFilters.map(
                                    (option: AnomalyFilterOptions, index) => (
                                        <Chip
                                            className="filter-chip"
                                            key={`${index}_${option.value}`}
                                            label={option.value}
                                            onDelete={() =>
                                                handleNodeClick(option)
                                            }
                                        />
                                    )
                                )
                            }
                            value={anomalyFilters}
                            onChange={(e, options) =>
                                handleOnChangeFilter(
                                    options as AnomalyFilterOptions[]
                                )
                            }
                        />
                    </Grid>
                )}
            </Grid>
            {loading ? (
                <AppLoadingIndicatorV1 />
            ) : !isEmpty(breakdownComparisonData) ? (
                React.Children.toArray(
                    breakdownComparisonData &&
                        breakdownComparisonData.map((data) => (
                            <>
                                <Divider />
                                <Treemap<AnomalyBreakdownComparisonData>
                                    colorChangeFactor="percentageDiff"
                                    name={data.column}
                                    tooltipElement={DimensionHeatmapTooltip}
                                    treemapData={formatTreemapData(data)}
                                    onDimensionClickHandler={handleNodeClick}
                                />
                            </>
                        ))
                )
            ) : (
                <Grid xs={12}>
                    <Typography variant="body1">
                        {t("message.no-data")}
                    </Typography>
                </Grid>
            )}
        </>
    );
};
