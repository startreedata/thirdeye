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
import { Box, Divider, Grid, TextField, Typography } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { toLower } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { MetricAggFunction } from "../../../rest/dto/metric.interfaces";
import {
    DatasetInfo,
    STAR_COLUMN,
} from "../../../utils/datasources/datasources.util";
import { useGetDatasourcesTree } from "../../../utils/datasources/use-get-datasources-tree.util";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2/alert-wizard-v2.styles";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { SelectMetricProps } from "./select-metric.interfaces";
import {
    determineDatasetInitialSelectionsFromServerData,
    GRANULARITY_OPTIONS,
} from "./select-metric.utils";

export const SelectMetric: FunctionComponent<SelectMetricProps> = ({
    alert,
    selectedTable,
    selectedAggregationFunction,
    selectedMetric,
    selectedGranularity,
    onSelectionChange,
}) => {
    const classes = useAlertWizardV2Styles();
    const { t } = useTranslation();

    const [isPinotInfraLoading, setIsPinotInfraLoading] = useState(true);

    const {
        datasetsInfo,
        getDatasourcesHook,
        getDatasetsHook,
        getMetricsHook,
    } = useGetDatasourcesTree();

    // Build the table configuration tree
    useEffect(() => {
        if (!datasetsInfo) {
            setIsPinotInfraLoading(true);

            return;
        }

        const [selectedDatasetInfo, metric] =
            determineDatasetInitialSelectionsFromServerData(
                datasetsInfo,
                alert
            );

        onSelectionChange(
            selectedDatasetInfo,
            metric,
            selectedAggregationFunction,
            selectedGranularity
        );

        setIsPinotInfraLoading(false);
    }, [datasetsInfo]);

    const handleMetricSelection = (metric: string): void => {
        if (!metric || !selectedTable) {
            return;
        }
        let aggregationColumn = selectedAggregationFunction;

        // If metric is * set the aggregation function to COUNT
        if (metric === STAR_COLUMN) {
            aggregationColumn = MetricAggFunction.COUNT;
        }

        onSelectionChange(
            selectedTable,
            metric,
            aggregationColumn,
            selectedGranularity
        );
    };

    const handleAggregationFunctionSelect = (
        aggregationFunction: MetricAggFunction
    ): void => {
        onSelectionChange(
            selectedTable,
            selectedMetric,
            aggregationFunction,
            selectedGranularity
        );
    };

    const handleGranularityChange = (
        _: unknown,
        granularityOption: { label: string; value: string }
    ): void => {
        onSelectionChange(
            selectedTable,
            selectedMetric,
            selectedAggregationFunction,
            granularityOption
        );
    };

    return (
        <>
            <LoadingErrorStateSwitch
                isError={
                    getDatasourcesHook.status === ActionStatus.Error ||
                    getDatasetsHook.status === ActionStatus.Error ||
                    getMetricsHook.status === ActionStatus.Error
                }
                isLoading={isPinotInfraLoading}
            >
                <Grid container>
                    <Grid item xs={12}>
                        <Typography variant="h5">
                            {t("label.select-a-metric")}
                        </Typography>
                    </Grid>

                    <InputSection
                        inputComponent={
                            <Autocomplete<DatasetInfo>
                                fullWidth
                                data-testId="datasource-select"
                                getOptionLabel={(option) =>
                                    option.dataset.name as string
                                }
                                noOptionsText={t(
                                    "message.no-options-available-entity",
                                    {
                                        entity: t("label.dataset"),
                                    }
                                )}
                                options={datasetsInfo || []}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{
                                            ...params.InputProps,
                                            /**
                                             * Override class name so
                                             * the size of input is smaller
                                             */
                                            className:
                                                classes.autoCompleteInput,
                                        }}
                                        placeholder={t(
                                            "message.select-dataset"
                                        )}
                                        variant="outlined"
                                    />
                                )}
                                renderOption={(
                                    option: DatasetInfo
                                ): JSX.Element => {
                                    return (
                                        <Box
                                            data-testId={`${toLower(
                                                option.dataset.name
                                            )}-datasource-option`}
                                        >
                                            <Typography variant="h6">
                                                {option.dataset.name}
                                            </Typography>
                                            <Typography variant="caption">
                                                {t("message.num-metrics", {
                                                    num: option.metrics.length,
                                                })}
                                            </Typography>
                                        </Box>
                                    );
                                }}
                                value={selectedTable}
                                onChange={(_, selectedTableInfo) => {
                                    if (!selectedTableInfo) {
                                        return;
                                    }

                                    onSelectionChange(
                                        selectedTableInfo,
                                        null,
                                        selectedAggregationFunction,
                                        selectedGranularity
                                    );
                                }}
                            />
                        }
                        label={t("label.dataset")}
                    />

                    <InputSection
                        inputComponent={
                            <Autocomplete<string>
                                fullWidth
                                data-testId="metric-select"
                                disabled={!selectedTable}
                                noOptionsText={t(
                                    "message.no-options-available-entity",
                                    {
                                        entity: t("label.metric"),
                                    }
                                )}
                                options={
                                    selectedTable
                                        ? selectedTable.metrics.map(
                                              (m) => m.name
                                          )
                                        : []
                                }
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{
                                            ...params.InputProps,
                                            /**
                                             * Override class name so
                                             * the size of input is smaller
                                             */
                                            className:
                                                classes.autoCompleteInput,
                                        }}
                                        placeholder={
                                            !selectedTable
                                                ? t(
                                                      "message.select-dataset-first"
                                                  )
                                                : t("message.select-metric")
                                        }
                                        variant="outlined"
                                    />
                                )}
                                value={selectedMetric}
                                onChange={(_, metric) => {
                                    metric && handleMetricSelection(metric);
                                }}
                            />
                        }
                        label={t("label.metric")}
                    />

                    <InputSection
                        inputComponent={
                            <Autocomplete
                                disableClearable
                                fullWidth
                                // Disable selection if selected column is *
                                disabled={selectedMetric === STAR_COLUMN}
                                options={
                                    selectedMetric === STAR_COLUMN
                                        ? [MetricAggFunction.COUNT]
                                        : [
                                              MetricAggFunction.SUM,
                                              MetricAggFunction.AVG,
                                              MetricAggFunction.COUNT,
                                              MetricAggFunction.MIN,
                                              MetricAggFunction.MAX,
                                          ]
                                }
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{
                                            ...params.InputProps,
                                            /**
                                             * Override class name so
                                             * the size of input is smaller
                                             */
                                            className:
                                                classes.autoCompleteInput,
                                        }}
                                        placeholder={t(
                                            "message.select-aggregation-function"
                                        )}
                                        variant="outlined"
                                    />
                                )}
                                value={selectedAggregationFunction || undefined}
                                onChange={(_, aggregationFunction) => {
                                    aggregationFunction &&
                                        handleAggregationFunctionSelect(
                                            aggregationFunction
                                        );
                                }}
                            />
                        }
                        label={`${t("label.aggregation-function")}`}
                    />

                    <Grid item xs={12}>
                        <Box padding={1}>
                            <Divider />
                        </Box>
                    </Grid>

                    <InputSection
                        inputComponent={
                            <Autocomplete
                                disableClearable
                                fullWidth
                                getOptionLabel={(option) => option.label}
                                options={GRANULARITY_OPTIONS}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        InputProps={{
                                            ...params.InputProps,
                                            /**
                                             * Override class name so
                                             * the size of input is smaller
                                             */
                                            className:
                                                classes.autoCompleteInput,
                                        }}
                                        placeholder={t(
                                            "label.select-granularity"
                                        )}
                                        variant="outlined"
                                    />
                                )}
                                value={selectedGranularity || undefined}
                                onChange={handleGranularityChange}
                            />
                        }
                        label="Granularity"
                    />
                </Grid>
            </LoadingErrorStateSwitch>
        </>
    );
};
