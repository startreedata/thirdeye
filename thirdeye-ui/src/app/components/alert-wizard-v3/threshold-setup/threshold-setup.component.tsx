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
    Grid,
    Slider,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    JSONEditorV1,
    PageContentsCardV1,
    SkeletonV1,
    useDialogProviderV1,
} from "../../../platform/components";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetDatasets } from "../../../rest/datasets/datasets.actions";
import { useGetDatasources } from "../../../rest/datasources/datasources.actions";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { MetricAggFunction } from "../../../rest/dto/metric.interfaces";
import { useGetMetrics } from "../../../rest/metrics/metrics.actions";
import {
    buildPinotDatasourcesTree,
    DatasetInfo,
} from "../../../utils/datasources/datasources.util";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2/alert-wizard-v2.styles";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { PreviewChart } from "../preview-chart/preview-chart.component";
import { ThresholdSetupProps } from "./threshold-setup.interfaces";
import { generateTemplateProperties } from "./threshold-setup.utils";

export const ThresholdSetup: FunctionComponent<ThresholdSetupProps> = ({
    onAlertPropertyChange,
    alert,
}) => {
    const classes = useAlertWizardV2Styles();
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();

    const advancedEditorAlertState = useState(alert);
    const setAdvancedEditorAlert = advancedEditorAlertState[1];
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

    const [datasetsInfo, setDatasetsInfo] = useState<DatasetInfo[] | null>(
        null
    );
    const [isPinotInfraLoading, setIsPinotInfraLoading] = useState(true);
    const [selectedTable, setSelectedTable] = useState<DatasetInfo | null>(
        null
    );
    const [selectedMetric, setSelectedMetric] = useState<string | null>(null);
    const [selectedAggregationFunction, setSelectedAggregationFunction] =
        useState<MetricAggFunction>(MetricAggFunction.SUM);

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

    const handleMetricSelection = (metric: string): void => {
        if (!metric || !selectedTable) {
            return;
        }

        setSelectedMetric(metric);

        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                ...generateTemplateProperties(
                    metric,
                    selectedTable.dataset,
                    selectedAggregationFunction
                ),
            },
        });
    };

    const handleAdvancedEditorOk = (configToReplace: EditableAlert): void => {
        onAlertPropertyChange(configToReplace, true);

        const newlySelectedDataset = datasetsInfo?.find((candidate) => {
            return (
                candidate.dataset.name ===
                    configToReplace.templateProperties?.dataset &&
                candidate.dataset.dataSource.name ===
                    configToReplace.templateProperties?.dataSource
            );
        });

        setSelectedTable(newlySelectedDataset || null);

        if (newlySelectedDataset) {
            setSelectedMetric(
                (configToReplace.templateProperties
                    ?.aggregationColumn as string) || null
            );
        } else {
            setSelectedMetric(null);
        }

        if (configToReplace.templateProperties?.aggregationFunction) {
            setSelectedAggregationFunction(
                configToReplace.templateProperties
                    ?.aggregationFunction as MetricAggFunction
            );
        }
    };

    const handleAdvancedEditorBtnClick = useCallback((): void => {
        showDialog({
            type: DialogType.CUSTOM,
            headerText: t("label.detection-configuration"),
            contents: (
                <JSONEditorV1<EditableAlert>
                    disableValidation
                    value={alert}
                    onChange={(updates) => {
                        setAdvancedEditorAlert(() => JSON.parse(updates));
                    }}
                />
            ),
            width: "md",
            okButtonText: t("label.apply-changes"),
            cancelButtonText: t("label.cancel"),
            onOk: () => {
                setAdvancedEditorAlert((current) => {
                    // Wait for previous state updates to finish before
                    // calling handleAdvancedEditorOk
                    handleAdvancedEditorOk(current);

                    return current;
                });
            },
        });
    }, [showDialog, alert]);

    const handleAggregationFunctionSelect = (
        aggregationFunction: MetricAggFunction
    ): void => {
        setSelectedAggregationFunction(
            aggregationFunction as MetricAggFunction
        );
        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                aggregationFunction: aggregationFunction,
            },
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
                        <Grid
                            container
                            alignItems="center"
                            justifyContent="space-between"
                        >
                            <Grid item>
                                <Typography variant="h5">
                                    {t("label.threshold-setup")}
                                </Typography>
                                <Typography variant="body2">
                                    {t("message.threshold-setup-description")}
                                </Typography>
                            </Grid>
                            <Grid item>
                                <Button
                                    color="primary"
                                    onClick={handleAdvancedEditorBtnClick}
                                >
                                    {t("label.advanced-editor")}
                                </Button>
                            </Grid>
                        </Grid>
                    </Grid>

                    <PreviewChart
                        alert={alert}
                        showLoadButton={!!selectedMetric}
                    />

                    <Grid item xs={12}>
                        <Box padding={1} />
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
                                                {t("message.num-metrics", {
                                                    num: option.metrics.length,
                                                })}
                                            </Typography>
                                        </li>
                                    );
                                }}
                                value={selectedTable}
                                onChange={(_, selectedTableInfo) => {
                                    if (!selectedTableInfo) {
                                        return;
                                    }

                                    setSelectedMetric(null);
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
                                options={[
                                    MetricAggFunction.SUM,
                                    MetricAggFunction.AVG,
                                    MetricAggFunction.COUNT,
                                    MetricAggFunction.MIN,
                                    MetricAggFunction.MAX,
                                ]}
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
                                            "message.select-aggregation-function"
                                        )}
                                        variant="outlined"
                                    />
                                )}
                                value={selectedAggregationFunction}
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

                    <InputSection
                        inputComponent={
                            <Slider
                                defaultValue={50}
                                marks={[
                                    {
                                        value: 0,
                                        label: "Low",
                                    },
                                    {
                                        value: 50,
                                        label: "Medium",
                                    },
                                    {
                                        value: 100,
                                        label: "High",
                                    },
                                ]}
                                step={1}
                            />
                        }
                        label={t("label.anomaly-sensitivity")}
                    />
                </Grid>
            </LoadingErrorStateSwitch>
        </PageContentsCardV1>
    );
};
