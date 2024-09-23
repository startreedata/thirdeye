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
    FormHelperText,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAlertInsight } from "../../../rest/alerts/alerts.actions";
import { useGetDatasets } from "../../../rest/datasets/datasets.actions";
import { useGetDatasources } from "../../../rest/datasources/datasources.actions";
import { MetricAggFunction } from "../../../rest/dto/metric.interfaces";
import { useGetMetrics } from "../../../rest/metrics/metrics.actions";
import { createAlertConfigForInsights } from "../../../utils/cohort-detector/cohort-detector.util";
import {
    buildPinotDatasourcesTree,
    DatasetInfo,
} from "../../../utils/datasources/datasources.util";
import { generateDateRangeMonthsFromNow } from "../../../utils/routes/routes.util";
import {
    SessionStorageKeys,
    useSessionStorage,
} from "../../../utils/storage/use-session-storage";
import { InputSectionV2 } from "../../form-basics/input-section-v2/input-section-v2.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeSelectorButton } from "../../time-range/v2/time-range-selector-button/time-range-selector-button.component";
import { DatasetDetailsProps } from "./dataset-details.interfaces";

export const DatasetDetails: FunctionComponent<DatasetDetailsProps> = ({
    title,
    subtitle,
    submitButtonLabel,
    onSearchButtonClick,
    initialSelectedMetric,
    initialSelectedDataset,
    initialSelectedDatasource,
    initialSelectedAggregationFunc,
    queryValue,
    setQueryValue,
}) => {
    const {
        datasources,
        getDatasources,
        status: getDatasourcesStatus,
    } = useGetDatasources();
    const {
        datasets,
        getDatasets,
        status: getDatasetsStatus,
    } = useGetDatasets();
    const {
        alertInsight,
        getAlertInsight,
        status: alertInsightRequestStatus,
    } = useGetAlertInsight();
    const { metrics, getMetrics, status: getMetricsStatus } = useGetMetrics();
    const { t } = useTranslation();

    const [shouldFetchInsight, setShouldFetchInsight] = useState(true);
    const [isPinotInfraLoading, setIsPinotInfraLoading] = useState(true);
    const [selectedStart, setSelectedStart] = useState<number>(
        generateDateRangeMonthsFromNow(3)[0]
    );
    const [selectedEnd, setSelectedEnd] = useState<number>(
        generateDateRangeMonthsFromNow(3)[1]
    );
    const [selectedPercentageFilter, setSelectedPercentageFilter] = useState(5);
    const [selectedTable, setSelectedTable] = useState<DatasetInfo | null>(
        null
    );
    const [selectedMetric, setSelectedMetric] = useState<string | null>(null);
    const selectedAggregationFunction: MetricAggFunction =
        initialSelectedAggregationFunc ?? MetricAggFunction.SUM;
    const [selectedDimensions, setSelectedDimensions] = useState<string[]>([]);

    useEffect(() => {
        getDatasets();
        getMetrics();
        getDatasources();
    }, []);

    // Store the selected dimensions till the flow is completed ...or the session is ended.
    const [storedDimensions, setStoredDimensions] = useSessionStorage<string[]>(
        SessionStorageKeys.SelectedDimensionsOnAlertFlow,
        []
    );

    useEffect(() => {
        if (selectedTable && storedDimensions.length > 0) {
            const dimensionsInTable = storedDimensions.filter((dim) =>
                selectedTable.dimensions.includes(dim)
            );
            setSelectedDimensions(dimensionsInTable);
            setStoredDimensions(dimensionsInTable);
        }
    }, [selectedTable]);

    // Build the table configuration tree
    useEffect(() => {
        if (!metrics || !datasets || !datasources) {
            setIsPinotInfraLoading(true);

            return;
        }

        const datasourceInfo = buildPinotDatasourcesTree(
            datasources,
            datasets,
            metrics
        );
        const datasetInfo = datasourceInfo.reduce(
            (previous: DatasetInfo[], dSource) => {
                return [...previous, ...dSource.tables];
            },
            []
        );

        if (
            initialSelectedDataset &&
            initialSelectedMetric &&
            initialSelectedDatasource
        ) {
            const matchingDataset = datasetInfo.find(
                (item) =>
                    item.dataset.name === initialSelectedDataset &&
                    item.datasource === initialSelectedDatasource
            );

            if (matchingDataset) {
                setSelectedTable(matchingDataset);
                setSelectedMetric(initialSelectedMetric);
                getAlertInsight({
                    alert: createAlertConfigForInsights(
                        matchingDataset.datasource,
                        matchingDataset.dataset.name,
                        initialSelectedMetric,
                        selectedAggregationFunction
                    ),
                });
            }
        }

        setIsPinotInfraLoading(false);
    }, [metrics, datasets, datasources]);

    useEffect(() => {
        if (shouldFetchInsight) {
            if (selectedTable && selectedMetric) {
                getAlertInsight({
                    alert: createAlertConfigForInsights(
                        selectedTable.datasource,
                        selectedTable.dataset.name,
                        selectedMetric,
                        selectedAggregationFunction
                    ),
                }).then((insights) => {
                    if (insights) {
                        if (selectedStart > insights.datasetEndTime) {
                            setSelectedStart(insights.datasetStartTime);
                        }

                        if (selectedEnd > insights.datasetEndTime) {
                            setSelectedEnd(insights.datasetEndTime);
                        }
                    }
                    setShouldFetchInsight(false);
                });
            }
        }
    }, [selectedTable, selectedMetric]);

    const handleSearchClick = (): void => {
        if (!selectedTable) {
            return;
        }

        const matchingMetricInfo = selectedTable.metrics.find(
            (m) => m.name === selectedMetric
        );

        if (!matchingMetricInfo) {
            return;
        }

        onSearchButtonClick({
            metric: matchingMetricInfo,
            start: selectedStart,
            end: selectedEnd,
            dimensions: selectedDimensions,
            query: queryValue,
            percentage: selectedPercentageFilter,
            aggregationFunction: selectedAggregationFunction,
            dataset: selectedTable.dataset,
            resultSize: 99, // Max out at 99
        });
    };

    return (
        <>
            <LoadingErrorStateSwitch
                isError={
                    getDatasourcesStatus === ActionStatus.Error ||
                    getDatasetsStatus === ActionStatus.Error ||
                    getMetricsStatus === ActionStatus.Error
                }
                isLoading={isPinotInfraLoading}
            >
                <Grid container>
                    <Grid item xs={12}>
                        <Box marginBottom={2}>
                            <Typography variant="h6">{title}</Typography>
                            {subtitle && (
                                <Typography variant="body2">
                                    {subtitle}
                                </Typography>
                            )}
                        </Box>
                    </Grid>
                    <Grid item xs={6}>
                        <InputSectionV2
                            inputComponent={
                                <Autocomplete
                                    fullWidth
                                    multiple
                                    disabled={!selectedTable}
                                    noOptionsText={t(
                                        "message.no-options-available-entity",
                                        {
                                            entity: t("label.dimensions"),
                                        }
                                    )}
                                    options={
                                        selectedTable
                                            ? selectedTable.dimensions
                                            : []
                                    }
                                    renderInput={(params) => (
                                        <TextField
                                            error={
                                                !!selectedTable &&
                                                !!selectedMetric &&
                                                selectedDimensions.length === 0
                                            }
                                            {...params}
                                            InputProps={{
                                                ...params.InputProps,
                                            }}
                                            placeholder={
                                                !selectedTable
                                                    ? t(
                                                          "message.select-dataset-first"
                                                      )
                                                    : t(
                                                          "message.select-dimensions"
                                                      )
                                            }
                                            variant="outlined"
                                        />
                                    )}
                                    value={selectedDimensions}
                                    onChange={(_, dimensions) => {
                                        setSelectedDimensions(dimensions || []);

                                        // Update selection in storage as well in case the
                                        // user returns to this page in the same flow
                                        setStoredDimensions(dimensions || []);
                                    }}
                                />
                            }
                            label={`${t("label.dimensions")}`}
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <InputSectionV2
                            inputComponent={
                                <TimeRangeSelectorButton
                                    fullWidth
                                    end={selectedEnd}
                                    maxDate={
                                        alertInsightRequestStatus ===
                                        ActionStatus.Done
                                            ? alertInsight?.datasetEndTime
                                            : undefined
                                    }
                                    minDate={
                                        alertInsightRequestStatus ===
                                        ActionStatus.Done
                                            ? alertInsight?.datasetStartTime
                                            : undefined
                                    }
                                    placeholder={t(
                                        "message.click-to-select-date-range"
                                    )}
                                    start={selectedStart}
                                    onChange={(start, end) => {
                                        setSelectedStart(start);
                                        setSelectedEnd(end);
                                    }}
                                />
                            }
                            label={t("label.date-range")}
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <InputSectionV2
                            helperLabel={`(${t("label.optional")})`}
                            inputComponent={
                                <>
                                    <TextField
                                        fullWidth
                                        type="text"
                                        value={queryValue}
                                        onChange={(e) => {
                                            setQueryValue(
                                                e.currentTarget.value
                                            );
                                        }}
                                    />
                                    <FormHelperText>
                                        {t("message.query-example")}
                                    </FormHelperText>
                                </>
                            }
                            label={`${t("label.query-filter")}`}
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <InputSectionV2
                            helperLabel={t("message.find-dimensions-that-are")}
                            inputComponent={
                                <>
                                    <TextField
                                        fullWidth
                                        type="number"
                                        value={selectedPercentageFilter}
                                        onChange={(e) => {
                                            setSelectedPercentageFilter(
                                                Number(e.currentTarget.value)
                                            );
                                        }}
                                    />
                                </>
                            }
                            label={`${t(
                                "label.contribution-percentage-for-the-selected-metrics"
                            )}`}
                        />
                    </Grid>
                    <InputSectionV2
                        inputComponent={
                            <Box>
                                <Button
                                    color="primary"
                                    disabled={
                                        !selectedTable ||
                                        !selectedMetric ||
                                        selectedDimensions.length === 0
                                    }
                                    onClick={handleSearchClick}
                                >
                                    {submitButtonLabel}
                                </Button>
                            </Box>
                        }
                    />
                </Grid>
            </LoadingErrorStateSwitch>
        </>
    );
};
