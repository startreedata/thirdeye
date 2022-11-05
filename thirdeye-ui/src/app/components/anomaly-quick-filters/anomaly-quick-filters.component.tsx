import { useMediaQuery, useTheme } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { getAllDatasets } from "../../rest/datasets/datasets.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Metric } from "../../rest/dto/metric.interfaces";
import { getAllMetrics } from "../../rest/metrics/metrics.rest";
import { getTimeRangeDuration } from "../../utils/time-range/time-range.util";
import { FilterOptionsAutoComplete } from "../filter-options-auto-complete/filter-options-auto-complete.component";
import { FilterOption } from "../filter-options-auto-complete/filter-options-auto-complete.interfaces";
import { useTimeRange } from "../time-range/time-range-provider/time-range-provider.component";
import {
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelector } from "../time-range/time-range-selector/time-range-selector/time-range-selector.component";
import {
    AnomalyFilterQueryStringKey,
    AnomalyQuickFiltersProps,
} from "./anomaly-quick-filter.interface";
import { useAnomalyQuickFilterStyles } from "./anomaly-quick-filters.styles";

function initializeSelected(
    searchParams: URLSearchParams,
    searchParamKey: string
): FilterOption | null {
    if (searchParams.has(searchParamKey)) {
        return {
            id: searchParams.get(searchParamKey) as string,
            label: searchParams.get(searchParamKey) as string,
        };
    }

    return null;
}

export const AnomalyQuickFilters: FunctionComponent<
    AnomalyQuickFiltersProps
> = ({ showTimeSelectorOnly }) => {
    const {
        timeRangeDuration,
        recentCustomTimeRangeDurations,
        setTimeRangeDuration,
    } = useTimeRange();
    const theme = useTheme();
    const { t } = useTranslation();
    const classes = useAnomalyQuickFilterStyles();
    const [searchParams, setSearchParams] = useSearchParams();
    const [selectedAlert, setSelectedAlert] = useState(
        initializeSelected(searchParams, AnomalyFilterQueryStringKey.ALERT)
    );
    const [selectedDataset, setSelectedDataset] = useState(
        initializeSelected(searchParams, AnomalyFilterQueryStringKey.METRIC)
    );
    const [selectedMetric, setSelectedMetric] = useState(
        initializeSelected(searchParams, AnomalyFilterQueryStringKey.METRIC)
    );

    const screenWidthSmUp = useMediaQuery(theme.breakpoints.up("sm"));

    const handleChange = (
        filter: FilterOption | null,
        queryParamKey: string,
        setFunc: (option: FilterOption | null) => void
    ): void => {
        if (filter) {
            searchParams.set(queryParamKey, filter.id.toString());
            setFunc(filter);
        } else {
            searchParams.delete(queryParamKey);
            setFunc(null);
        }

        setSearchParams(searchParams);
    };

    useEffect(() => {
        setSelectedAlert(
            initializeSelected(searchParams, AnomalyFilterQueryStringKey.ALERT)
        );
        setSelectedDataset(
            initializeSelected(
                searchParams,
                AnomalyFilterQueryStringKey.DATASET
            )
        );
        setSelectedMetric(
            initializeSelected(searchParams, AnomalyFilterQueryStringKey.METRIC)
        );
    }, [searchParams]);

    const onHandleTimeRangeChange = (
        timeRangeDuration: TimeRangeDuration
    ): void => {
        setTimeRangeDuration(timeRangeDuration);
        searchParams.set(
            TimeRangeQueryStringKey.TIME_RANGE,
            timeRangeDuration.timeRange
        );
        searchParams.set(
            TimeRangeQueryStringKey.START_TIME,
            timeRangeDuration.startTime.toString()
        );
        searchParams.set(
            TimeRangeQueryStringKey.END_TIME,
            timeRangeDuration.endTime.toString()
        );
        setSearchParams(searchParams);
    };

    const onHandleRefresh = (): void => {
        onHandleTimeRangeChange(
            getTimeRangeDuration(timeRangeDuration.timeRange)
        );
    };

    // Return uniq metric as filter options
    const getUniqMetrics = async (): Promise<Metric[]> => {
        const metrics = await getAllMetrics();

        return metrics.reduce(
            (acc, metric) =>
                acc.find((m) => m.name === metric.name)
                    ? acc
                    : [...acc, metric],
            [] as Metric[]
        );
    };

    return (
        <div className={classes.root}>
            <div className={classes.filterGroup}>
                {!showTimeSelectorOnly && (
                    <>
                        <div className={classes.dataGridToolbarSearch}>
                            <FilterOptionsAutoComplete<Metric>
                                fetchOptions={getUniqMetrics}
                                formatOptionFromServer={(rawOption) => {
                                    return {
                                        id: rawOption.name,
                                        label: rawOption.name,
                                    };
                                }}
                                label={t("label.metric")}
                                name={t("label.metric")}
                                selected={selectedMetric}
                                onSelectionChange={(selected) =>
                                    handleChange(
                                        selected,
                                        AnomalyFilterQueryStringKey.METRIC,
                                        setSelectedMetric
                                    )
                                }
                            />
                        </div>
                        <div className={classes.dataGridToolbarSearch}>
                            <FilterOptionsAutoComplete<Dataset>
                                fetchOptions={getAllDatasets}
                                formatOptionFromServer={(rawOption) => {
                                    return {
                                        id: rawOption.name,
                                        label: rawOption.name,
                                    };
                                }}
                                label={t("label.dataset")}
                                name={t("label.dataset")}
                                selected={selectedDataset}
                                onSelectionChange={(selected) =>
                                    handleChange(
                                        selected,
                                        AnomalyFilterQueryStringKey.DATASET,
                                        setSelectedDataset
                                    )
                                }
                            />
                        </div>
                        <div className={classes.dataGridToolbarSearch}>
                            <FilterOptionsAutoComplete<Alert>
                                fetchOptions={getAllAlerts}
                                formatOptionFromServer={(rawOption) => {
                                    return {
                                        id: rawOption.id,
                                        label: rawOption.name,
                                    };
                                }}
                                formatSelectedAfterOptionsFetch={(
                                    selected: FilterOption,
                                    options: FilterOption[]
                                ) => {
                                    const selectedId = Number(selected.id);
                                    const matching = options.find(
                                        (item) => item.id === selectedId
                                    );

                                    return matching || selected;
                                }}
                                label={t("label.alert")}
                                name={t("label.alert")}
                                selected={selectedAlert}
                                onSelectionChange={(selected) =>
                                    handleChange(
                                        selected,
                                        AnomalyFilterQueryStringKey.ALERT,
                                        setSelectedAlert
                                    )
                                }
                            />
                        </div>
                    </>
                )}
            </div>
            <div>
                <TimeRangeSelector
                    hideTimeRange={!screenWidthSmUp}
                    recentCustomTimeRangeDurations={
                        recentCustomTimeRangeDurations
                    }
                    timeRangeDuration={timeRangeDuration}
                    onChange={onHandleTimeRangeChange}
                    onRefresh={onHandleRefresh}
                />
            </div>
        </div>
    );
};
