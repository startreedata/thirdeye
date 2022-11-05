/*
 * Copyright 2022 StarTree Inc
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
    Divider,
    FormHelperText,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetDatasets } from "../../../rest/datasets/datasets.actions";
import { useGetDatasources } from "../../../rest/datasources/datasources.actions";
import { useGetMetrics } from "../../../rest/metrics/metrics.actions";
import {
    buildPinotDatasourcesTree,
    DatasetInfo,
} from "../../../utils/datasources/datasources.util";
import { generateDateRangeMonthsFromNow } from "../../../utils/routes/routes.util";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2/alert-wizard-v2.styles";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeSelectorButton } from "../../time-range/v2/time-range-selector-button/time-range-selector-button.component";
import { DatasetDetailsProps } from "./dataset-details.interfaces";

export const DatasetDetails: FunctionComponent<DatasetDetailsProps> = ({
    onSearchButtonClick,
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
    const { metrics, getMetrics, status: getMetricsStatus } = useGetMetrics();
    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();

    const [datasetsInfo, setDatasetsInfo] = useState<DatasetInfo[] | null>(
        null
    );
    const [isPinotInfraLoading, setIsPinotInfraLoading] = useState(true);
    const [selectedStart, setSelectedStart] = useState<number>(
        generateDateRangeMonthsFromNow(3)[0]
    );
    const [selectedEnd, setSelectedEnd] = useState<number>(
        generateDateRangeMonthsFromNow(3)[1]
    );
    const [selectedPercentageFilter, setSelectedPercentageFilter] =
        useState(50);
    const [selectedResultSize, setSelectedResultSize] = useState(25);
    const [selectedTable, setSelectedTable] = useState<DatasetInfo | null>(
        null
    );
    const [selectedMetric, setSelectedMetric] = useState<string | null>(null);
    const [selectedDimensions, setSelectedDimensions] = useState<string[]>([]);
    const [queryValue, setQueryValue] = useState<string>("");

    useEffect(() => {
        getDatasets();
        getMetrics();
        getDatasources();
    }, []);

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

        setDatasetsInfo(datasetInfo);

        setIsPinotInfraLoading(false);
    }, [metrics, datasets, datasources]);

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
            metricId: matchingMetricInfo.id,
            start: selectedStart,
            end: selectedEnd,
            dimensions: selectedDimensions,
            query: queryValue,
            resultSize: selectedResultSize,
            percentage: selectedPercentageFilter,
        });
    };

    return (
        <PageContentsCardV1>
            <LoadingErrorStateSwitch
                isError={
                    getDatasourcesStatus === ActionStatus.Error ||
                    getDatasetsStatus === ActionStatus.Error ||
                    getMetricsStatus === ActionStatus.Error
                }
                isLoading={isPinotInfraLoading}
                loadingState={
                    <>
                        <SkeletonV1 animation="pulse" />
                        <SkeletonV1 animation="pulse" />
                        <SkeletonV1 animation="pulse" />
                    </>
                }
            >
                <Grid container>
                    <Grid item xs={12}>
                        <Box marginBottom={2}>
                            <Typography variant="h5">
                                {t("label.dataset-details")}
                            </Typography>
                        </Box>
                    </Grid>

                    <InputSection
                        inputComponent={
                            <Autocomplete<DatasetInfo>
                                fullWidth
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
                                            // Override class name so the size of input is smaller
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
                                        <li>
                                            <Typography variant="h6">
                                                {option.dataset.name}
                                            </Typography>
                                            <Typography variant="caption">
                                                From{" "}
                                                <strong>
                                                    {option.datasource}
                                                </strong>{" "}
                                                datasource with{" "}
                                                <strong>
                                                    {option.metrics.length}
                                                </strong>{" "}
                                                metrics
                                            </Typography>
                                        </li>
                                    );
                                }}
                                value={selectedTable}
                                onChange={(_, selectedTableInfo) => {
                                    setSelectedMetric(null);
                                    setSelectedDimensions([]);

                                    selectedTableInfo &&
                                        setSelectedTable(selectedTableInfo);
                                }}
                            />
                        }
                        label={t("label.dataset")}
                    />

                    <InputSection
                        inputComponent={
                            <Autocomplete<string>
                                fullWidth
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
                                            // Override class name so the size of input is smaller
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
                                    metric && setSelectedMetric(metric);
                                }}
                            />
                        }
                        label={t("label.metric")}
                    />

                    <InputSection
                        helperLabel={`(${t("label.optional")})`}
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
                                        {...params}
                                        InputProps={{
                                            ...params.InputProps,
                                            // Override class name so the size of input is smaller
                                            className:
                                                classes.autoCompleteInput,
                                        }}
                                        placeholder={
                                            !selectedTable
                                                ? t(
                                                      "message.select-dataset-first"
                                                  )
                                                : t("message.select-dimensions")
                                        }
                                        variant="outlined"
                                    />
                                )}
                                value={selectedDimensions}
                                onChange={(_, dimensions) => {
                                    setSelectedDimensions(dimensions || []);
                                }}
                            />
                        }
                        label={`${t("label.dimensions")}`}
                    />

                    <Grid item xs={12}>
                        <Divider />
                    </Grid>

                    <Grid item xs={12}>
                        <InputSection
                            helperLabel={t(
                                "message.select-a-date-range-to-filter-data-by"
                            )}
                            inputComponent={
                                <TimeRangeSelectorButton
                                    fullWidth
                                    end={selectedEnd}
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

                    <Grid item xs={12}>
                        <Divider />
                    </Grid>

                    <InputSection
                        helperLabel={`(${t("label.optional")})`}
                        inputComponent={
                            <>
                                <TextField
                                    fullWidth
                                    type="text"
                                    value={queryValue}
                                    onChange={(e) => {
                                        setQueryValue(e.currentTarget.value);
                                    }}
                                />
                                <FormHelperText>
                                    {t("message.query-example")}
                                </FormHelperText>
                            </>
                        }
                        label={`${t("label.query-filter")}`}
                    />

                    <Grid item xs={12}>
                        <Divider />
                    </Grid>

                    <InputSection
                        helperLabel={t("message.find-cohorts-that-are")}
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
                        label={`${t("label.cohort-size-filter")}`}
                    />

                    <InputSection
                        helperLabel={t(
                            "message.return-this-many-results-at-most"
                        )}
                        inputComponent={
                            <>
                                <TextField
                                    fullWidth
                                    type="number"
                                    value={selectedResultSize}
                                    onChange={(e) => {
                                        setSelectedResultSize(
                                            Number(e.currentTarget.value)
                                        );
                                    }}
                                />
                            </>
                        }
                        label={`${t("label.result-size")}`}
                    />

                    <InputSection
                        inputComponent={
                            <Box textAlign="right">
                                <Button
                                    color="primary"
                                    disabled={!selectedTable || !selectedMetric}
                                    onClick={handleSearchClick}
                                >
                                    {t("label.find-cohorts")}
                                </Button>
                            </Box>
                        }
                    />
                </Grid>
            </LoadingErrorStateSwitch>
        </PageContentsCardV1>
    );
};
