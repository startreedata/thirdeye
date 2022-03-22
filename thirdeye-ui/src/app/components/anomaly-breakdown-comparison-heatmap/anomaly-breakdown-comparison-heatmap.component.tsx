import {
    Box,
    Card,
    CardContent,
    Chip,
    Divider,
    Grid,
    MenuItem,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { HierarchyNode } from "d3-hierarchy";
import { isEmpty, isString, pull } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { formatDateAndTimeV1 } from "../../platform/utils";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAnomalyMetricBreakdown } from "../../rest/rca/rca.actions";
import { EMPTY_STRING_DISPLAY } from "../../utils/anomalies/anomalies.util";
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
    formatTreemapData,
    OFFSET_TO_HUMAN_READABLE,
    OFFSET_TO_MILLISECONDS,
    summarizeDimensionValueData,
} from "./anomaly-breakdown-comparison-heatmap.utils";
import { DimensionHeatmapTooltip } from "./dimension-heatmap-tooltip/dimension-heatmap-tooltip.component";

export const AnomalyBreakdownComparisonHeatmap: FunctionComponent<
    AnomalyBreakdownComparisonHeatmapProps
> = ({
    anomalyId,
    anomaly,
    shouldTruncateText = true,
}: AnomalyBreakdownComparisonHeatmapProps) => {
    const classes = useAnomalyBreakdownComparisonHeatmapStyles();
    const { t } = useTranslation();
    const {
        anomalyMetricBreakdown,
        getMetricBreakdown,
        status: anomalyBreakdownReqStatus,
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
    const [comparisonOffset, setComparisonWeekOffset] =
        useState<AnomalyBreakdownAPIOffsetValues>(
            AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO
        );
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        if (!anomalyMetricBreakdown) {
            setBreakdownComparisonData(null);

            return;
        }

        const breakdownComparisonDataByDimensionColumn: AnomalyBreakdownComparisonDataByDimensionColumn[] =
            [];

        if (anomalyFilterOptions.length === 0) {
            let optionsMenu: AnomalyFilterOption[] = [];
            Object.keys(anomalyMetricBreakdown.current.breakdown).forEach(
                (dimensionColumnName) => {
                    const options = Object.keys(
                        anomalyMetricBreakdown.current.breakdown[
                            dimensionColumnName
                        ]
                    ).map((value) => ({
                        key: dimensionColumnName,
                        value,
                    }));
                    optionsMenu = [...optionsMenu, ...options];
                }
            );
            setAnomalyFilterOptions(optionsMenu);
        }

        Object.keys(anomalyMetricBreakdown.current.breakdown).forEach(
            (dimensionColumnName) => {
                const [currentTotal, currentDimensionValuesData] =
                    summarizeDimensionValueData(
                        anomalyMetricBreakdown.current.breakdown[
                            dimensionColumnName
                        ]
                    );
                const [baselineTotal, baselineDimensionValuesData] =
                    summarizeDimensionValueData(
                        anomalyMetricBreakdown.baseline.breakdown[
                            dimensionColumnName
                        ]
                    );
                const dimensionComparisonData: {
                    [key: string]: AnomalyBreakdownComparisonData;
                } = {};

                Object.keys(currentDimensionValuesData).forEach(
                    (dimension: string) => {
                        const currentDataForDimension =
                            currentDimensionValuesData[dimension];
                        const baselineDataForDimension =
                            baselineDimensionValuesData[dimension] || {};
                        const baselineMetricValue =
                            baselineDataForDimension.count || 0;

                        dimensionComparisonData[dimension] = {
                            current: currentDataForDimension.count,
                            baseline: baselineMetricValue,
                            metricValueDiff:
                                currentDataForDimension.count -
                                baselineMetricValue,
                            metricValueDiffPercentage: null,
                            currentContributionPercentage:
                                currentDataForDimension.percentage || 0,
                            baselineContributionPercentage:
                                baselineDataForDimension.percentage || 0,
                            contributionDiff:
                                (currentDataForDimension.percentage || 0) -
                                (baselineDataForDimension.percentage || 0),
                            currentTotalCount: currentTotal,
                            baselineTotalCount: baselineTotal,
                        };

                        if (baselineMetricValue > 0) {
                            dimensionComparisonData[
                                dimension
                            ].metricValueDiffPercentage =
                                ((currentDataForDimension.count -
                                    baselineMetricValue) /
                                    baselineMetricValue) *
                                100;
                        }
                    }
                );

                breakdownComparisonDataByDimensionColumn.push({
                    column: dimensionColumnName,
                    dimensionComparisonData,
                });
            }
        );
        setBreakdownComparisonData(breakdownComparisonDataByDimensionColumn);
    }, [anomalyMetricBreakdown]);

    useEffect(() => {
        getMetricBreakdown(anomalyId, {
            baselineOffset: comparisonOffset,
            filters: [
                ...anomalyFilters.map(
                    (option) => `${option.key}=${option.value}`
                ),
            ],
        });
    }, [anomalyId, comparisonOffset, anomalyFilters]);

    useEffect(() => {
        if (anomalyBreakdownReqStatus === ActionStatus.Error) {
            notify(
                NotificationTypeV1.Error,
                t("message.error-while-fetching", {
                    entity: t("label.heatmap-data"),
                })
            );
        }
    }, [anomalyBreakdownReqStatus]);

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
        setAnomalyFilters([...resultantFilters]);
    };

    const handleNodeFilterOnDelete = (node: AnomalyFilterOption): void => {
        const resultantFilters = pull(anomalyFilters, node);
        setAnomalyFilters([...resultantFilters]);
    };

    const handleOnChangeFilter = (options: AnomalyFilterOption[]): void => {
        setAnomalyFilters([...options]);
    };

    const colorChangeValueAccessor = (
        node: TreemapData<AnomalyBreakdownComparisonData>
    ): number => {
        if (node.extraData) {
            return node.extraData.contributionDiff * 100;
        }

        return node.size;
    };

    const baselineOptions: {
        key: AnomalyBreakdownAPIOffsetValues;
        description: string;
    }[] = [];
    Object.values(AnomalyBreakdownAPIOffsetValues).forEach(
        (offsetKey: AnomalyBreakdownAPIOffsetValues) => {
            if (offsetKey !== AnomalyBreakdownAPIOffsetValues.CURRENT) {
                baselineOptions.push({
                    key: offsetKey,
                    description: OFFSET_TO_HUMAN_READABLE[offsetKey],
                });
            }
        }
    );

    const onHandleComparisonOffsetSelection = (
        e: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
    ): void => {
        setComparisonWeekOffset(
            e.target.value as AnomalyBreakdownAPIOffsetValues
        );
    };

    return (
        <Card variant="outlined">
            <CardContent>
                <Grid container justifyContent="space-between">
                    <Grid item sm={6} xs={12}>
                        <Typography gutterBottom component="h2" variant="h5">
                            Heatmap of Change in Contribution
                        </Typography>
                    </Grid>
                    <Grid item sm={6} xs={12}>
                        <Grid container spacing={0}>
                            <Grid item sm={6} xs={12}>
                                <Box p="10.5px 0">
                                    <label>
                                        <strong>
                                            {t("label.baseline-week-offset")}:
                                        </strong>
                                    </label>
                                </Box>
                            </Grid>
                            <Grid item sm={6} xs={12}>
                                <TextField
                                    fullWidth
                                    select
                                    size="small"
                                    value={comparisonOffset}
                                    onChange={onHandleComparisonOffsetSelection}
                                >
                                    {baselineOptions.map((option) => (
                                        <MenuItem
                                            key={option.key}
                                            value={option.key}
                                        >
                                            {option.description}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>
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
                                    {formatDateAndTimeV1(anomaly.startTime)}
                                    <strong> to </strong>
                                    {formatDateAndTimeV1(anomaly.endTime)}
                                </div>
                            </Grid>
                            <Grid item xs={6}>
                                <div>
                                    <strong>
                                        &quot;{t("label.baseline")}&quot;
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
                                    {formatDateAndTimeV1(
                                        anomaly.startTime -
                                            OFFSET_TO_MILLISECONDS[
                                                comparisonOffset
                                            ]
                                    )}
                                    <strong> to </strong>
                                    {formatDateAndTimeV1(
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
        </Card>
    );
};
