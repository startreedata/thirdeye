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
    Divider,
    FormHelperText,
    Grid,
    Link,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { sortBy } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { InputSection } from "../../../components/form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { BaselineOffsetSelection } from "../../../components/rca/analysis-tabs/baseline-offset-selection/baseline-offset-selection.component";
import { InvestigationPreview } from "../../../components/rca/investigation-preview/investigation-preview.component";
import { PageContentsCardV1 } from "../../../platform/components";
import { formatDateV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAlertInsight } from "../../../rest/alerts/alerts.actions";
import { useGetDatasets } from "../../../rest/datasets/datasets.actions";
import { useGetDatasources } from "../../../rest/datasources/datasources.actions";
import { MetricAggFunction } from "../../../rest/dto/metric.interfaces";
import { useGetMetrics } from "../../../rest/metrics/metrics.actions";
import {
    baselineOffsetToMilliseconds,
    comparisonOffsetReadableValue,
} from "../../../utils/anomaly-breakdown/anomaly-breakdown.util";
import { createAlertConfigForInsights } from "../../../utils/cohort-detector/cohort-detector.util";
import {
    buildPinotDatasourcesTree,
    DatasetInfo,
    STAR_COLUMN,
} from "../../../utils/datasources/datasources.util";
import { generateDateRangeMonthsFromNow } from "../../../utils/routes/routes.util";
import {
    SessionStorageKeys,
    useSessionStorage,
} from "../../../utils/storage/use-session-storage";
import { InvestigationContext } from "../investigation-state-tracker-container-page/investigation-state-tracker.interfaces";

export const MetricsDrillDown: FunctionComponent = () => {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const context = useOutletContext<InvestigationContext>();
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
        // alertInsight,
        getAlertInsight,
        // status: alertInsightRequestStatus,
    } = useGetAlertInsight();
    const { metrics, getMetrics, status: getMetricsStatus } = useGetMetrics();

    const [datasetsInfo, setDatasetsInfo] = useState<DatasetInfo[] | null>(
        null
    );
    const [queryValue, setQueryValue] = useState("");
    const [shouldFetchInsight, setShouldFetchInsight] = useState(true);
    const [isPinotInfraLoading, setIsPinotInfraLoading] = useState(true);
    const [selectedStart, setSelectedStart] = useState<number>(
        generateDateRangeMonthsFromNow(3)[0]
    );
    const [selectedEnd, setSelectedEnd] = useState<number>(
        generateDateRangeMonthsFromNow(3)[1]
    );
    const [selectedTable, setSelectedTable] = useState<DatasetInfo | null>(
        null
    );
    const [selectedMetric, setSelectedMetric] = useState<string | null>(null);
    const [selectedAggregationFunction, setSelectedAggregationFunction] =
        useState<MetricAggFunction>(MetricAggFunction.SUM);
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

        // if (
        //     initialSelectedDataset &&
        //     initialSelectedMetric &&
        //     initialSelectedDatasource
        // ) {
        //     const matchingDataset = datasetInfo.find(
        //         (item) =>
        //             item.dataset.name === initialSelectedDataset &&
        //             item.datasource === initialSelectedDatasource
        //     );

        //     if (matchingDataset) {
        //         setSelectedTable(matchingDataset);
        //         setSelectedMetric(initialSelectedMetric);
        //         getAlertInsight({
        //             alert: createAlertConfigForInsights(
        //                 matchingDataset.datasource,
        //                 matchingDataset.dataset.name,
        //                 initialSelectedMetric,
        //                 selectedAggregationFunction
        //             ),
        //         });
        //     }
        // }

        setDatasetsInfo(
            sortBy(datasetInfo, [(d) => d.dataset.name.toLowerCase()])
        );

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

    const [comparisonOffset, setComparisonOffset] = useState(() => {
        return searchParams.get("baselineWeekOffset") ?? "P1W";
    });

    const handleBaselineChange = (newValue: string): void => {
        setComparisonOffset(newValue);
        searchParams.set("baselineWeekOffset", newValue);
        setSearchParams(searchParams);
    };

    return (
        <>
            <Grid item xs={9}>
                <PageContentsCardV1 style={{ borderRadius: 8 }}>
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
                                <Typography variant="body2">
                                    {t(
                                        "message.create-a-query-confirm-results"
                                    )}
                                </Typography>
                            </Grid>
                            <Grid item xs={12}>
                                <Grid container>
                                    <Grid item xs={4}>
                                        <InputSection
                                            fullWidth
                                            inputComponent={
                                                <Autocomplete<DatasetInfo>
                                                    fullWidth
                                                    getOptionLabel={(option) =>
                                                        option.dataset
                                                            .name as string
                                                    }
                                                    noOptionsText={t(
                                                        "message.no-options-available-entity",
                                                        {
                                                            entity: t(
                                                                "label.dataset"
                                                            ),
                                                        }
                                                    )}
                                                    options={datasetsInfo || []}
                                                    renderInput={(params) => (
                                                        <TextField
                                                            {...params}
                                                            InputProps={{
                                                                ...params.InputProps,
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
                                                                    {
                                                                        option
                                                                            .dataset
                                                                            .name
                                                                    }
                                                                </Typography>
                                                                <Typography variant="caption">
                                                                    From{" "}
                                                                    <strong>
                                                                        {
                                                                            option.datasource
                                                                        }
                                                                    </strong>{" "}
                                                                    datasource
                                                                    with{" "}
                                                                    <strong>
                                                                        {
                                                                            option
                                                                                .metrics
                                                                                .length
                                                                        }
                                                                    </strong>{" "}
                                                                    metrics
                                                                </Typography>
                                                            </li>
                                                        );
                                                    }}
                                                    value={selectedTable}
                                                    onChange={(
                                                        _,
                                                        selectedTableInfo
                                                    ) => {
                                                        if (
                                                            !selectedTableInfo
                                                        ) {
                                                            return;
                                                        }

                                                        setSelectedDimensions(
                                                            []
                                                        );
                                                        setSelectedMetric(null);
                                                        setSelectedTable(
                                                            selectedTableInfo
                                                        );
                                                        setShouldFetchInsight(
                                                            true
                                                        );
                                                    }}
                                                />
                                            }
                                            label={t("label.dataset")}
                                        />
                                    </Grid>
                                    <Grid item xs={4}>
                                        <InputSection
                                            fullWidth
                                            inputComponent={
                                                <Autocomplete<string>
                                                    fullWidth
                                                    disabled={!selectedTable}
                                                    noOptionsText={t(
                                                        "message.no-options-available-entity",
                                                        {
                                                            entity: t(
                                                                "label.metric"
                                                            ),
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
                                                            }}
                                                            placeholder={
                                                                !selectedTable
                                                                    ? t(
                                                                          "message.select-dataset-first"
                                                                      )
                                                                    : t(
                                                                          "message.select-metric"
                                                                      )
                                                            }
                                                            variant="outlined"
                                                        />
                                                    )}
                                                    value={selectedMetric}
                                                    onChange={(_, metric) => {
                                                        if (metric) {
                                                            const isStartColumn =
                                                                metric ===
                                                                STAR_COLUMN;

                                                            setSelectedMetric(
                                                                metric
                                                            );

                                                            if (isStartColumn) {
                                                                setSelectedAggregationFunction(
                                                                    MetricAggFunction.COUNT
                                                                );
                                                            }
                                                        }
                                                    }}
                                                />
                                            }
                                            label={t("label.metric")}
                                        />
                                    </Grid>
                                    <Grid item xs={4}>
                                        <InputSection
                                            fullWidth
                                            inputComponent={
                                                <Autocomplete
                                                    disableClearable
                                                    fullWidth
                                                    disabled={
                                                        selectedMetric ===
                                                        STAR_COLUMN
                                                    }
                                                    options={
                                                        selectedMetric ===
                                                        STAR_COLUMN
                                                            ? [
                                                                  MetricAggFunction.COUNT,
                                                              ]
                                                            : [
                                                                  MetricAggFunction.SUM,
                                                                  MetricAggFunction.COUNT,
                                                              ]
                                                    }
                                                    renderInput={(params) => (
                                                        <TextField
                                                            {...params}
                                                            InputProps={{
                                                                ...params.InputProps,
                                                            }}
                                                            placeholder={t(
                                                                "message.select-aggregation-function"
                                                            )}
                                                            variant="outlined"
                                                        />
                                                    )}
                                                    value={
                                                        selectedAggregationFunction
                                                    }
                                                    onChange={(
                                                        _,
                                                        aggregationFunction
                                                    ) => {
                                                        if (
                                                            aggregationFunction
                                                        ) {
                                                            setSelectedAggregationFunction(
                                                                aggregationFunction as MetricAggFunction
                                                            );
                                                        }
                                                    }}
                                                />
                                            }
                                            label={`${t(
                                                "label.aggregation-function"
                                            )}`}
                                        />
                                    </Grid>
                                    <Grid item xs={12}>
                                        <Divider />
                                    </Grid>
                                    <Grid item xs={6}>
                                        <InputSection
                                            fullWidth
                                            inputComponent={
                                                <Autocomplete
                                                    fullWidth
                                                    multiple
                                                    disabled={!selectedTable}
                                                    noOptionsText={t(
                                                        "message.no-options-available-entity",
                                                        {
                                                            entity: t(
                                                                "label.dimensions"
                                                            ),
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
                                                                selectedDimensions.length ===
                                                                    0
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
                                                    onChange={(
                                                        _,
                                                        dimensions
                                                    ) => {
                                                        setSelectedDimensions(
                                                            dimensions || []
                                                        );
                                                        setStoredDimensions(
                                                            dimensions || []
                                                        );
                                                    }}
                                                />
                                            }
                                            label={`${t("label.dimensions")}`}
                                        />
                                    </Grid>
                                    <Grid item xs={6}>
                                        <InputSection
                                            fullWidth
                                            helperLabel={`(${t(
                                                "label.optional"
                                            )})`}
                                            inputComponent={
                                                <>
                                                    <TextField
                                                        fullWidth
                                                        type="text"
                                                        value={queryValue}
                                                        onChange={(e) => {
                                                            setQueryValue(
                                                                e.currentTarget
                                                                    .value
                                                            );
                                                        }}
                                                    />
                                                    <FormHelperText>
                                                        {t(
                                                            "message.query-example"
                                                        )}
                                                    </FormHelperText>
                                                </>
                                            }
                                            label={`${t("label.query-filter")}`}
                                        />
                                    </Grid>
                                    <Grid item xs={12}>
                                        <Divider />
                                    </Grid>
                                </Grid>
                            </Grid>
                            <Grid item xs={12}>
                                <Grid
                                    container
                                    alignItems="center"
                                    justifyContent="space-between"
                                >
                                    <Grid item xs={4}>
                                        <BaselineOffsetSelection
                                            inStart
                                            baselineOffset={comparisonOffset}
                                            hasReloadButton={false}
                                            label={t(
                                                "label.dimensions-changed-from-the-last"
                                            )}
                                            onBaselineOffsetChange={
                                                handleBaselineChange
                                            }
                                        />
                                    </Grid>
                                    <Grid item xs={4}>
                                        <div>
                                            <strong>
                                                &quot;{t("label.current")}&quot;
                                            </strong>{" "}
                                            Data Date Range
                                        </div>
                                        <div>
                                            {formatDateV1(
                                                context.anomaly?.startTime
                                            )}
                                            <strong> to </strong>
                                            {formatDateV1(
                                                context.anomaly?.endTime
                                            )}
                                        </div>
                                    </Grid>
                                    <Grid item xs={4}>
                                        <div>
                                            <strong>
                                                &quot;{t("label.baseline")}
                                                &quot;
                                            </strong>
                                            <span>
                                                {" "}
                                                Data Date Range (
                                                {comparisonOffsetReadableValue(
                                                    comparisonOffset
                                                )}
                                                )
                                            </span>
                                        </div>
                                        <div>
                                            {formatDateV1(
                                                context.anomaly?.startTime -
                                                    baselineOffsetToMilliseconds(
                                                        comparisonOffset
                                                    )
                                            )}
                                            <strong> to </strong>
                                            {formatDateV1(
                                                context.anomaly?.endTime -
                                                    baselineOffsetToMilliseconds(
                                                        comparisonOffset
                                                    )
                                            )}
                                        </div>
                                    </Grid>
                                </Grid>
                            </Grid>
                            <Grid item xs={12}>
                                <Box sx={{ mb: 2 }}>
                                    <Typography variant="body2">
                                        {t("label.write-a-query")}
                                    </Typography>
                                </Box>
                                <TextField fullWidth multiline rows={4} />
                                <Box sx={{ my: 2 }}>
                                    <Typography variant="body2">
                                        {t(
                                            "message.for-a-better-understanding"
                                        )}
                                        <Link>
                                            {" "}
                                            {t("label.read-our-documentation")}
                                        </Link>
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item xs={12}>
                                <Button
                                    color="primary"
                                    size="small"
                                    variant="outlined"
                                >
                                    {t("label.run-query")}
                                </Button>
                            </Grid>
                        </Grid>
                    </LoadingErrorStateSwitch>
                </PageContentsCardV1>

                <Grid item xs={12}>
                    <InvestigationPreview
                        alertInsight={context.alertInsight}
                        anomaly={context.anomaly}
                        investigation={context.investigation}
                        onInvestigationChange={context.onInvestigationChange}
                    />
                </Grid>
            </Grid>
        </>
    );
};
