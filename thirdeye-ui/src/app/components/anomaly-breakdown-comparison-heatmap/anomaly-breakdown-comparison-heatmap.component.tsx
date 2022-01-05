import {
    Box,
    Card,
    CardContent,
    CardHeader,
    Chip,
    Divider,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { isEmpty, isString, map, pull } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAnomalyMetricBreakdown } from "../../rest/rca/rca.actions";
import { EMPTY_STRING_DISPLAY } from "../../utils/anomalies/anomalies.util";
import { formatDateAndTime } from "../../utils/date-time/date-time.util";
import { Treemap } from "../visualizations/treemap/treemap.component";
import { TreemapData } from "../visualizations/treemap/treemap.interfaces";
import {
    AnomalyBreakdownComparisonData,
    AnomalyBreakdownComparisonDataByDimensionColumn,
    AnomalyBreakdownComparisonHeatmapProps,
    AnomalyFilterOption,
    SummarizeDataFunctionParams,
    SummaryData,
} from "./anomaly-breakdown-comparison-heatmap.interfaces";
import { useAnomalyBreakdownComparisonHeatmapStyles } from "./anomaly-breakdown-comparison-heatmap.styles";
import { DimensionHeatmapTooltip } from "./dimension-heatmap-tooltip/dimension-heatmap-tooltip.component";

const WEEK_IN_MILLISECONDS = 604800000;
const OFFSET_TO_MILLISECONDS = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: 0,
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: WEEK_IN_MILLISECONDS,
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: 2 * WEEK_IN_MILLISECONDS,
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: 3 * WEEK_IN_MILLISECONDS,
};
const OFFSET_TO_HUMAN_READABLE = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: "",
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: "One Week Ago",
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: "Two Weeks Ago",
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: "Three Weeks Ago",
};

function summarizeDimensionValueData(
    dimensionValueData: SummarizeDataFunctionParams
): [number, SummaryData] {
    const summarized: SummaryData = {};
    if (isEmpty(dimensionValueData)) {
        return [0, summarized];
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
            totalCount,
        };
    });

    return [totalCount, summarized];
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
    anomaly,
}: AnomalyBreakdownComparisonHeatmapProps) => {
    const classes = useAnomalyBreakdownComparisonHeatmapStyles();
    const { t } = useTranslation();
    const {
        anomalyMetricBreakdown: anomalyBreakdownCurrent,
        getMetricBreakdown: getMetricBreakdownForCurrent,
        status: anomalyBreakdownCurrentReqStatus,
    } = useGetAnomalyMetricBreakdown();
    const {
        anomalyMetricBreakdown: anomalyBreakdownComparison,
        getMetricBreakdown: getMetricBreakdownForComparison,
        status: anomalyBreakdownComparisonReqStatus,
    } = useGetAnomalyMetricBreakdown();
    const [breakdownComparisonData, setBreakdownComparisonData] = useState<
        AnomalyBreakdownComparisonDataByDimensionColumn[] | null
    >(null);
    const [anomalyFilters, setAnomalyFilters] = useState<AnomalyFilterOption[]>(
        []
    );
    const [anomalyFilterOptions, setAnomalyFilterOptions] = useState<
        AnomalyFilterOption[]
    >([]);

    useEffect(() => {
        if (!anomalyBreakdownCurrent || !anomalyBreakdownComparison) {
            setBreakdownComparisonData(null);

            return;
        }

        const breakdownComparisonDataByDimensionColumn: AnomalyBreakdownComparisonDataByDimensionColumn[] = [];

        if (!anomalyFilterOptions.length) {
            let optionsMenu: AnomalyFilterOption[] = [];
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
            const [
                currentTotal,
                currentDimensionValuesData,
            ] = summarizeDimensionValueData(
                anomalyBreakdownCurrent[dimensionColumnName]
            );
            const [
                comparisonTotal,
                comparisonDimensionValuesData,
            ] = summarizeDimensionValueData(
                anomalyBreakdownComparison[dimensionColumnName]
            );
            const dimensionComparisonData: {
                [key: string]: AnomalyBreakdownComparisonData;
            } = {};

            Object.keys(currentDimensionValuesData).forEach(
                (dimension: string) => {
                    const currentDataForDimension =
                        currentDimensionValuesData[dimension];
                    const comparisonDataForDimension =
                        comparisonDimensionValuesData[dimension] || {};
                    dimensionComparisonData[dimension] = {
                        current: currentDataForDimension.count,
                        currentPercentage:
                            currentDataForDimension.percentage || 0,
                        comparison: comparisonDataForDimension.count || 0,
                        comparisonPercentage:
                            comparisonDataForDimension.percentage || 0,
                        percentageDiff:
                            (currentDataForDimension.percentage || 0) -
                            (comparisonDataForDimension.percentage || 0),
                        currentTotalCount: currentTotal,
                        comparisonTotalCount: comparisonTotal,
                    };
                }
            );

            breakdownComparisonDataByDimensionColumn.push({
                column: dimensionColumnName,
                dimensionComparisonData,
            });
        });
        setBreakdownComparisonData(breakdownComparisonDataByDimensionColumn);
    }, [anomalyBreakdownCurrent, anomalyBreakdownComparison]);

    useEffect(() => {
        getMetricBreakdownForCurrent(anomalyId, {
            timezone: "UTC",
            filters: [
                ...anomalyFilters.map(
                    (option) => `${option.key}=${option.value}`
                ),
            ],
        });

        getMetricBreakdownForComparison(anomalyId, {
            timezone: "UTC",
            offset: comparisonOffset,
            filters: [
                ...anomalyFilters.map(
                    (option) => `${option.key}=${option.value}`
                ),
            ],
        });
    }, [anomalyId, comparisonOffset, anomalyFilters]);

    const handleNodeClick = (node: AnomalyFilterOption): void => {
        if (!node) {
            return;
        }

        let resultantFilters: AnomalyFilterOption[] = [];

        if (anomalyFilters.includes(node)) {
            resultantFilters = pull(anomalyFilters, node);
        } else {
            resultantFilters = [...anomalyFilters, node];
        }
        setAnomalyFilters([...resultantFilters]);
    };

    const handleOnChangeFilter = (options: AnomalyFilterOption[]): void => {
        setAnomalyFilters([...options]);
    };

    const colorChangeValueAccessor = (
        node: TreemapData<AnomalyBreakdownComparisonData>
    ): number => {
        if (node.extraData) {
            return node.extraData.percentageDiff * 100;
        }

        return node.size;
    };

    return (
        <Card variant="outlined">
            <CardHeader
                title="Heatmap of Change in Contribution"
                titleTypographyProps={{ variant: "h5" }}
            />

            <CardContent>
                {breakdownComparisonData &&
                    breakdownComparisonData.length > 0 &&
                    anomaly && (
                        <Grid container>
                            <Grid item xs={12}>
                                <Typography variant="h6">
                                    Tooltip Reference
                                </Typography>
                            </Grid>
                            <Grid item xs={6}>
                                <div>
                                    <strong>
                                        &quot;{t("label.current")}&quot;
                                    </strong>{" "}
                                    Data Date Range
                                </div>
                                <div>
                                    {formatDateAndTime(anomaly.startTime)}
                                    <strong> to </strong>
                                    {formatDateAndTime(anomaly.endTime)}
                                </div>
                            </Grid>
                            <Grid item xs={6}>
                                <div>
                                    <strong>
                                        &quot;{t("label.comparison")}&quot;
                                    </strong>
                                    <span>
                                        {" "}
                                        Data Date Range (
                                        {
                                            OFFSET_TO_HUMAN_READABLE[
                                                comparisonOffset
                                            ]
                                        }
                                        )
                                    </span>
                                </div>
                                <div>
                                    {formatDateAndTime(
                                        anomaly.startTime -
                                            OFFSET_TO_MILLISECONDS[
                                                comparisonOffset
                                            ]
                                    )}
                                    <strong> to </strong>
                                    {formatDateAndTime(
                                        anomaly.endTime -
                                            OFFSET_TO_MILLISECONDS[
                                                comparisonOffset
                                            ]
                                    )}
                                </div>
                            </Grid>

                            <Grid item xs={12}>
                                <Divider />
                            </Grid>
                        </Grid>
                    )}
                {anomalyFilterOptions && (
                    <Grid container className={classes.filtersContainer}>
                        <Grid item xs={12}>
                            <Box mt={2}>
                                <Typography variant="h6">
                                    Filter Data Controls
                                </Typography>
                            </Box>
                        </Grid>
                        <Grid item xs={12}>
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
                                        label={t("label.add-a-dimension")}
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
                                                    handleNodeClick(option)
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
                    </Grid>
                )}
            </CardContent>
            <CardContent>
                {/* Loading Indicator when requests are in flight */}
                {(anomalyBreakdownCurrentReqStatus === ActionStatus.Working ||
                    anomalyBreakdownComparisonReqStatus ===
                        ActionStatus.Working) && (
                    <Box pb={20} pt={20}>
                        <AppLoadingIndicatorV1 />
                    </Box>
                )}

                {/* If breakdownComparisonData is not empty render treemaps */}
                {anomalyBreakdownCurrentReqStatus === ActionStatus.Done &&
                    anomalyBreakdownComparisonReqStatus === ActionStatus.Done &&
                    !isEmpty(breakdownComparisonData) &&
                    React.Children.toArray(
                        breakdownComparisonData &&
                            breakdownComparisonData.map((data) => (
                                <>
                                    <Divider />
                                    <Treemap<AnomalyBreakdownComparisonData>
                                        colorChangeValueAccessor={
                                            colorChangeValueAccessor
                                        }
                                        name={data.column}
                                        tooltipElement={DimensionHeatmapTooltip}
                                        treemapData={formatTreemapData(data)}
                                        onDimensionClickHandler={
                                            handleNodeClick
                                        }
                                    />
                                </>
                            ))
                    )}

                {/* Indicate no data if breakdown data is missing and requests are complete */}
                {anomalyBreakdownCurrentReqStatus === ActionStatus.Done &&
                    anomalyBreakdownComparisonReqStatus === ActionStatus.Done &&
                    isEmpty(breakdownComparisonData) && (
                        <Grid xs={12}>
                            <Box pb={20} pt={20}>
                                <Typography align="center" variant="body1">
                                    {t("message.no-data")}
                                </Typography>
                            </Box>
                        </Grid>
                    )}
            </CardContent>
        </Card>
    );
};
