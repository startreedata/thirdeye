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
    Chip,
    Divider,
    Grid,
    TextareaAutosize,
    TextField,
    ThemeProvider,
    Tooltip,
    Typography,
} from "@material-ui/core";
import { KeyboardArrowDown, KeyboardArrowUp } from "@material-ui/icons";
import DimensionImage from "../../../../assets/images/dimensions.png";
import { ReactComponent as FilterListRoundedIcon } from "../../../platform/assets/images/filter-icon.svg";
import AddCircleOutline from "@material-ui/icons/AddCircleOutline";
import { Alert, AlertTitle, Autocomplete } from "@material-ui/lab";
import DoneAllIcon from "@material-ui/icons/DoneAll";
import { isNil, toLower } from "lodash";
import { DateTime, Duration } from "luxon";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { AvailableAlgorithmOption } from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.interfaces";
import { generateAvailableAlgorithmOptions } from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.utils";
import {
    generateTemplateProperties,
    GranularityValue,
    GRANULARITY_OPTIONS,
    GRANULARITY_OPTIONS_TOOLTIP,
} from "../../../components/alert-wizard-v3/select-metric/select-metric.utils";
import { ThresholdSetup } from "../../../components/alert-wizard-v3/threshold-setup/threshold-setup-v2.component";
import { InputSectionV2 } from "../../../components/form-basics/input-section-v2/input-section-v2.component";
import { RadioSection } from "../../../components/form-basics/radio-section-v2/radio-section.component";
import { RadioSectionOptions } from "../../../components/form-basics/radio-section-v2/radio-section.interfaces";
import { TimeRangeButtonWithContext } from "../../../components/time-range/time-range-button-with-context-v2/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    JSONEditorV2,
    PageContentsCardV1,
    PageHeaderActionsV1,
} from "../../../platform/components";
import { ColorV1 } from "../../../platform/utils/material-ui/color.util";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import {
    EditableAlert,
    TemplatePropertiesObject,
} from "../../../rest/dto/alert.interfaces";
import {
    AnomalyDetectionOptions,
    MetricAggFunction,
    SelectDimensionsOptions,
} from "../../../rest/dto/metric.interfaces";
import {
    createAlertEvaluation,
    determineTimezoneFromAlertInEvaluation,
} from "../../../utils/alerts/alerts.util";
import {
    DatasetInfo,
    STAR_COLUMN,
} from "../../../utils/datasources/datasources.util";
import { useGetDatasourcesTree } from "../../../utils/datasources/use-get-datasources-tree.util";
import { AlertCreatedGuidedPageOutletContext } from "../../alerts-create-guided-page/alerts-create-guided-page.interfaces";
import { AlertCompositeFiltersModal } from "../../../components/alert-composite-filters-modal/alert-composite-filters-modal.component";
import { CreateAlertModal } from "../../../components/create-alert-modal/create-alert-modal.component";
import { HelpDrawerV1 } from "../../../components/help-drawer-v1/help-drawer-v1.component";
import { alertsBasicHelpCards } from "../../../components/help-drawer-v1/help-drawer-card-contents.utils";
import { Icon } from "@iconify/react";
import { getAlertsAllPath } from "../../../utils/routes/routes.util";
import {
    easyAlertStyles,
    crateAlertPageTheme,
} from "./alerts-create-easy-page.styles";
import { AdditonalFiltersDrawer } from "../../../components/additional-filters-drawer/additional-filters-drawer.component";
import { getAvailableFilterOptions } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.utils";
import { AnomaliesFilterConfiguratorRenderConfigs } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.interfaces";
import { ColumnsDrawer } from "../../../components/columns-drawer/columns-drawer.component";

const PROPERTIES_TO_COPY = [
    "dataSource",
    "dataset",
    "aggregationColumn",
    "aggregationFunction",
    "monitoringGranularity",
    "enumerationItems",
    "queryFilters",
];

const ALERT_TEMPLATE_FOR_EVALUATE = "startree-threshold";
const ALERT_TEMPLATE_FOR_EVALUATE_DX = "startree-threshold-dx";

export const AlertsCreateEasyPage: FunctionComponent = () => {
    const classes = easyAlertStyles();

    const { t } = useTranslation();
    const navigate = useNavigate();

    const [searchParams, setSearchParams] = useSearchParams();
    const [showAdvancedOptions, setShowAdvancedOptions] = useState(false);
    const [queryFilters, setQueryFilters] = useState("");

    const [showSQLWhere, setShowSQLWhere] = useState(false);
    const [enumerations, setEnumerations] = useState(false);
    const [dimension, setDimension] = useState<string | null>(null);

    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    const {
        onAlertPropertyChange,
        alertTemplates,
        isMultiDimensionAlert,
        alertInsight,
        alertRecommendations,
        alert,
    } = useOutletContext<AlertCreatedGuidedPageOutletContext>();
    const { datasetsInfo } = useGetDatasourcesTree();

    const [selectedTable, setSelectedTable] = useState<DatasetInfo | null>(
        null
    );
    const [selectedMetric, setSelectedMetric] = useState<string | null>(null);
    const [aggregationFunction, setAggregationFunction] = useState<
        string | null
    >(null);
    const [granularity, setGranularity] = useState<GranularityValue | null>(
        null
    );
    const [anomalyDetection, setAnomalyDetection] = useState<string | null>(
        null
    );
    const [editedDatasource, setEditedDatasource] = useState("");
    const [enumerators, setEnumerators] = useState({});
    const [algorithmOption, setAlgorithmOption] =
        useState<AvailableAlgorithmOption | null>(null);

    const [compositeFilters, setCompositeFilters] =
        useState<TemplatePropertiesObject | null>(null);
    const [openCompositeFilterModal, setOpenCompositeFilterModal] =
        useState(false);
    const [openCreateAlertModal, setOpenCreateAlertModal] = useState(false);
    const [openViewColumnsListDrawer, setOpenViewColumnsListDrawer] =
        useState(false);

    useEffect(() => {
        if (alert.templateProperties?.dataset && datasetsInfo) {
            const dataSource =
                datasetsInfo.find(
                    (item) =>
                        alert.templateProperties?.dataset === item.dataset.name
                ) || null;
            if (dataSource) {
                const metrics =
                    dataSource.metrics.find(
                        (item) =>
                            item.name ===
                            alert.templateProperties?.aggregationColumn
                    )?.name || t("label.custom-metric-aggregation");
                setSelectedTable(dataSource);
                setSelectedMetric(metrics || null);
                setEditedDatasource(
                    String(alert.templateProperties?.aggregationColumn)
                );
                setAggregationFunction(
                    alert.templateProperties.aggregationFunction as string
                );
                setGranularity(
                    alert.templateProperties
                        .monitoringGranularity as GranularityValue
                );
                setAlgorithmOption(
                    recommendedAlertTemplateFirst.find((item) => {
                        const name = isMultiDimensionAlert
                            ? item.algorithmOption
                                  .alertTemplateForMultidimension
                            : item.algorithmOption.alertTemplate;

                        return alert.template?.name === name;
                    }) || null
                );
                setAnomalyDetection(
                    alert.templateProperties.enumerationItems
                        ? AnomalyDetectionOptions.COMPOSITE
                        : AnomalyDetectionOptions.SINGLE
                );
                setCompositeFilters(alert.templateProperties);
                setQueryFilters(
                    String(alert.templateProperties?.queryFilters) || ""
                );
            }
        }
    }, [datasetsInfo]);

    const isCreateButtonDisabled = useMemo(
        () =>
            !selectedTable ||
            !selectedMetric ||
            !aggregationFunction ||
            !granularity ||
            !anomalyDetection ||
            !algorithmOption,
        [
            selectedTable,
            selectedMetric,
            aggregationFunction,
            granularity,
            anomalyDetection,
            algorithmOption,
        ]
    );
    const alertTemplateForEvaluate = useMemo(() => {
        const alertTemplateToFind = isMultiDimensionAlert
            ? ALERT_TEMPLATE_FOR_EVALUATE_DX
            : ALERT_TEMPLATE_FOR_EVALUATE;

        return alertTemplates.find((alertTemplateCandidate) => {
            return alertTemplateCandidate.name === alertTemplateToFind;
        });
    }, [alertTemplates, alert, isMultiDimensionAlert]);

    const availableConfigurations = useMemo(() => {
        if (!alertTemplateForEvaluate) {
            return undefined;
        }

        return getAvailableFilterOptions(alertTemplateForEvaluate, t);
    }, [alertTemplateForEvaluate]);

    const handleGranularityChange = (
        _: unknown,
        item: { label: string; value: GranularityValue }
    ): void => {
        const prevGranularity = granularity;
        setGranularity(item.value);
        if (
            prevGranularity &&
            granularity &&
            !Duration.fromISO(granularity).equals(
                Duration.fromISO(prevGranularity)
            )
        ) {
            const newStartTime = startTime;
            let newEndTime = DateTime.fromMillis(newStartTime)
                .plus({
                    milliseconds: Duration.fromISO(granularity).toMillis() * 30,
                })
                .toMillis();

            if (alertInsight?.datasetEndTime) {
                newEndTime = Math.min(newEndTime, alertInsight?.datasetEndTime);
            }

            searchParams.set(
                TimeRangeQueryStringKey.START_TIME,
                newStartTime.toString()
            );
            searchParams.set(
                TimeRangeQueryStringKey.END_TIME,
                newEndTime.toString()
            );

            setSearchParams(searchParams);
        }
    };

    const getAggregationOptions = (
        values: Array<string>
    ): RadioSectionOptions[] => {
        const options: RadioSectionOptions[] = [];
        values.map((item) =>
            options.push({
                value: item,
                label: item,
                onClick: () => setAggregationFunction(item),
                tooltipText: item,
            })
        );

        return options;
    };

    const handleAnomalyDetectionChange = (item: string): void => {
        const copied = { ...alert };
        delete copied.templateProperties?.queryFilters;
        delete copied.templateProperties?.enumerationItems;
        setCompositeFilters(null);
        setAlertConfigForPreview(copied);
        onAlertPropertyChange(copied);
        setAnomalyDetection(item);
    };

    const getAnomalyDetectionOptions = (
        values: Array<string>
    ): RadioSectionOptions[] => {
        const options: RadioSectionOptions[] = [];
        values.map((item) =>
            options.push({
                value: item,
                label: item,
                onClick: () => handleAnomalyDetectionChange(item),
                tooltipText: item,
            })
        );

        return options;
    };

    const getSelectDimensionsOptions = (
        values: Array<string>
    ): RadioSectionOptions[] => {
        const options: RadioSectionOptions[] = [];
        values.map((item) =>
            options.push({
                value: item,
                label: item,
                onClick: () => setDimension(item),
                tooltipText: item,
            })
        );

        return options;
    };

    const recommendedAlertTemplate = useMemo(() => {
        if (alertRecommendations && alertRecommendations.length > 0) {
            return alertRecommendations[0]?.alert.template?.name;
        }

        return undefined;
    }, [alertRecommendations]);

    const alertTemplateOptions = useMemo(() => {
        return generateAvailableAlgorithmOptions(
            alertTemplates.map((a: AlertTemplate) => a.name)
        ).filter((option) =>
            isMultiDimensionAlert
                ? option.hasMultidimension
                : option.hasAlertTemplate
        );
    }, [alertTemplates]);

    const recommendedAlertTemplateFirst = useMemo(() => {
        const cloned = alertTemplateOptions.filter((c) => {
            return isMultiDimensionAlert
                ? c.algorithmOption.alertTemplateForMultidimension !==
                      recommendedAlertTemplate
                : c.algorithmOption.alertTemplate !== recommendedAlertTemplate;
        });

        const recommendedAlertTemplateOption = alertTemplateOptions.find(
            (c) => {
                return isMultiDimensionAlert
                    ? c.algorithmOption.alertTemplateForMultidimension ===
                          recommendedAlertTemplate
                    : c.algorithmOption.alertTemplate ===
                          recommendedAlertTemplate;
            }
        );

        if (recommendedAlertTemplateOption) {
            cloned.unshift(recommendedAlertTemplateOption);
        }

        return cloned;
    }, [alertTemplates, recommendedAlertTemplate]);

    useEffect(() => {
        let isCustomMetrics = false;
        if (selectedMetric === t("label.custom-metric-aggregation")) {
            isCustomMetrics = true;
        }
        if (
            selectedMetric &&
            selectedTable &&
            granularity &&
            (aggregationFunction || (isCustomMetrics && editedDatasource))
        ) {
            onAlertPropertyChange({
                template: {
                    name: isMultiDimensionAlert
                        ? algorithmOption?.algorithmOption
                              .alertTemplateForMultidimension
                        : algorithmOption?.algorithmOption?.alertTemplate,
                },
                templateProperties: {
                    ...alert.templateProperties,
                    ...generateTemplateProperties(
                        isCustomMetrics ? editedDatasource : selectedMetric,
                        selectedTable?.dataset,
                        aggregationFunction || "",
                        granularity
                    ),
                    queryFilters: queryFilters,
                },
            });
            handleReloadPreviewClick();
        }
    }, [
        selectedMetric,
        selectedTable,
        granularity,
        aggregationFunction,
        algorithmOption,
        queryFilters,
    ]);

    const { getEvaluation } = useGetEvaluation();

    const [alertConfigForPreview, setAlertConfigForPreview] =
        useState<EditableAlert>(() => {
            const workingAlert = createNewStartingAlert();

            workingAlert.template = {
                name: alertTemplateForEvaluate?.name,
            };

            PROPERTIES_TO_COPY.forEach((propKey) => {
                if (alert.templateProperties[propKey]) {
                    workingAlert.templateProperties[propKey] =
                        alert.templateProperties[propKey];
                }
            });

            workingAlert.templateProperties.min = 0;
            workingAlert.templateProperties.max = 0;

            return workingAlert;
        });

    const handleApplyAdvancedOptions = (
        fieldData: TemplatePropertiesObject
    ): void => {
        // handle advance options fields data
        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                ...fieldData,
            },
        });
        setShowAdvancedOptions(false);
    };

    const fetchAlertEvaluation = (start: number, end: number): void => {
        const copiedAlert = { ...alertConfigForPreview };
        delete copiedAlert.id;
        getEvaluation(createAlertEvaluation(copiedAlert, start, end));
    };
    // Update the preview config if selections change
    useEffect(() => {
        let isCustomMetrics = false;
        if (selectedMetric === t("label.custom-metric-aggregation")) {
            isCustomMetrics = true;
        }
        if (
            !selectedTable ||
            !selectedMetric ||
            !granularity ||
            (!aggregationFunction && !(isCustomMetrics && editedDatasource))
        ) {
            return;
        }

        setAlertConfigForPreview((currentConfig) => {
            const copied = {
                ...currentConfig,
                template: {
                    name: alertTemplateForEvaluate?.name,
                },
            };

            copied.templateProperties = {
                ...copied.templateProperties,
                ...generateTemplateProperties(
                    isCustomMetrics ? editedDatasource : selectedMetric,
                    selectedTable?.dataset,
                    aggregationFunction || "",
                    granularity
                ),
                queryFilters: queryFilters,
            };

            copied.templateProperties.min = 0;
            copied.templateProperties.max = 0;

            return copied;
        });
    }, [
        selectedTable,
        selectedMetric,
        granularity,
        aggregationFunction,
        alertTemplateForEvaluate,
        queryFilters,
    ]);

    const handleReloadPreviewClick = (): void => {
        if ((!startTime || !endTime) && alertInsight) {
            // If start or end is missing and there exists an alert insight
            fetchAlertEvaluation(
                alertInsight.defaultStartTime,
                alertInsight.defaultEndTime
            );
        } else {
            fetchAlertEvaluation(startTime, endTime);
        }
    };
    const recommendedAlertConfigMatchingTemplate = useMemo(() => {
        if (alertRecommendations && alert.template?.name) {
            return alertRecommendations.find(
                (candidate) =>
                    candidate.alert.template?.name === alert.template?.name
            );
        }

        return undefined;
    }, [alertRecommendations, alert]);

    const doesAlertHaveRecommendedValues = useMemo(() => {
        let hasValues = true;

        if (!recommendedAlertConfigMatchingTemplate) {
            return false;
        }

        Object.keys(
            recommendedAlertConfigMatchingTemplate.alert.templateProperties
        ).forEach((k) => {
            hasValues =
                hasValues &&
                recommendedAlertConfigMatchingTemplate.alert.templateProperties[
                    k
                ] === alert.templateProperties[k];
        });

        return hasValues;
    }, [recommendedAlertConfigMatchingTemplate, alert]);

    const handleTuneAlertClick = (): void => {
        if (!recommendedAlertConfigMatchingTemplate) {
            return;
        }
        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                ...recommendedAlertConfigMatchingTemplate.alert
                    .templateProperties,
            },
        });
    };

    const selectedAlertTemplate = useMemo(() => {
        return alertTemplates.find((alertTemplateCandidate) => {
            return alertTemplateCandidate.name === alert.template?.name;
        });
    }, [alertTemplates, alert]);

    const onUpdateCompositeFiltersChange = (
        template: TemplatePropertiesObject
    ): void => {
        setCompositeFilters(template);
    };

    return (
        <>
            <ThemeProvider theme={crateAlertPageTheme}>
                <Grid item xs={12}>
                    <PageContentsCardV1 className={classes.container}>
                        <Grid container>
                            <Grid item xs={12}>
                                <Box marginBottom={2}>
                                    <Grid
                                        container
                                        alignContent="center"
                                        justifyContent="space-between"
                                    >
                                        <Grid item xs={12}>
                                            <Box display="flex">
                                                <Typography
                                                    className={classes.header}
                                                    variant="h5"
                                                >
                                                    {t("label.alert-wizard")}
                                                </Typography>
                                                <PageHeaderActionsV1>
                                                    <HelpDrawerV1
                                                        cards={
                                                            alertsBasicHelpCards
                                                        }
                                                        title={`${t(
                                                            "label.need-help"
                                                        )}?`}
                                                        trigger={(
                                                            handleOpen
                                                        ) => (
                                                            <Button
                                                                className={
                                                                    classes.infoButton
                                                                }
                                                                color="primary"
                                                                size="small"
                                                                variant="outlined"
                                                                onClick={
                                                                    handleOpen
                                                                }
                                                            >
                                                                <Box
                                                                    component="span"
                                                                    mr={1}
                                                                >
                                                                    {t(
                                                                        "label.need-help"
                                                                    )}
                                                                </Box>
                                                                <Box
                                                                    component="span"
                                                                    display="flex"
                                                                >
                                                                    <Icon
                                                                        fontSize={
                                                                            24
                                                                        }
                                                                        icon="mdi:question-mark-circle-outline"
                                                                    />
                                                                </Box>
                                                            </Button>
                                                        )}
                                                    />
                                                </PageHeaderActionsV1>
                                            </Box>
                                            <Box>
                                                <Typography variant="body2">
                                                    {t(
                                                        "message.create-your-first-step-filling-fields"
                                                    )}
                                                </Typography>
                                            </Box>
                                        </Grid>

                                        <Grid item xs={12}>
                                            <Grid container>
                                                <Grid item xs={4}>
                                                    <InputSectionV2
                                                        description={t(
                                                            "message.select-dataset-to-monitor-and-detect-anomalies"
                                                        )}
                                                        inputComponent={
                                                            <Autocomplete<DatasetInfo>
                                                                fullWidth
                                                                data-testId="datasource-select"
                                                                getOptionLabel={(
                                                                    option
                                                                ) =>
                                                                    option
                                                                        .dataset
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
                                                                options={
                                                                    datasetsInfo ||
                                                                    []
                                                                }
                                                                renderInput={(
                                                                    params
                                                                ) => (
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
                                                                        <Box
                                                                            data-testId={`${toLower(
                                                                                option
                                                                                    .dataset
                                                                                    .name
                                                                            )}-datasource-option`}
                                                                        >
                                                                            <Typography variant="h6">
                                                                                {
                                                                                    option
                                                                                        .dataset
                                                                                        .name
                                                                                }
                                                                            </Typography>
                                                                            <Typography variant="caption">
                                                                                {t(
                                                                                    "message.num-metrics",
                                                                                    {
                                                                                        num: option
                                                                                            .metrics
                                                                                            .length,
                                                                                    }
                                                                                )}
                                                                            </Typography>
                                                                        </Box>
                                                                    );
                                                                }}
                                                                value={
                                                                    selectedTable
                                                                }
                                                                onChange={(
                                                                    _,
                                                                    selectedTableInfo
                                                                ) => {
                                                                    if (
                                                                        !selectedTableInfo
                                                                    ) {
                                                                        return;
                                                                    }
                                                                    setSelectedTable(
                                                                        selectedTableInfo
                                                                    );
                                                                    setSelectedMetric(
                                                                        null
                                                                    );
                                                                }}
                                                            />
                                                        }
                                                        label={t(
                                                            "label.dataset"
                                                        )}
                                                    />
                                                </Grid>
                                                <Grid item xs={4}>
                                                    <InputSectionV2
                                                        description={t(
                                                            "message.select-metric-to-identify-unusual-changes-when-it-occurs"
                                                        )}
                                                        inputComponent={
                                                            <Autocomplete<string>
                                                                fullWidth
                                                                data-testId="metric-select"
                                                                disabled={
                                                                    !selectedTable
                                                                }
                                                                groupBy={(
                                                                    option
                                                                ) =>
                                                                    option ===
                                                                    t(
                                                                        "label.custom-metric-aggregation"
                                                                    )
                                                                        ? "Y"
                                                                        : "N"
                                                                }
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
                                                                        ? (function () {
                                                                              const a =
                                                                                  selectedTable.metrics.map(
                                                                                      (
                                                                                          m
                                                                                      ) =>
                                                                                          m.name
                                                                                  );
                                                                              a.unshift(
                                                                                  t(
                                                                                      "label.custom-metric-aggregation"
                                                                                  )
                                                                              );

                                                                              return a;
                                                                          })()
                                                                        : []
                                                                }
                                                                renderGroup={(
                                                                    params
                                                                ) => (
                                                                    <li
                                                                        key={
                                                                            params.key
                                                                        }
                                                                    >
                                                                        {
                                                                            params.children
                                                                        }
                                                                        <Divider />
                                                                    </li>
                                                                )}
                                                                renderInput={(
                                                                    params
                                                                ) => (
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
                                                                value={
                                                                    selectedMetric
                                                                }
                                                                onChange={(
                                                                    _,
                                                                    metric
                                                                ) => {
                                                                    metric &&
                                                                        setSelectedMetric(
                                                                            metric
                                                                        );
                                                                }}
                                                            />
                                                        }
                                                        label={t(
                                                            "label.metric"
                                                        )}
                                                    />
                                                </Grid>
                                            </Grid>
                                        </Grid>
                                        {selectedMetric && (
                                            <Grid item xs={12}>
                                                <Grid container>
                                                    {selectedMetric !==
                                                    t(
                                                        "label.custom-metric-aggregation"
                                                    ) ? (
                                                        <Grid item xs={12}>
                                                            <RadioSection
                                                                defaultValue={
                                                                    aggregationFunction ||
                                                                    undefined
                                                                }
                                                                label={t(
                                                                    "label.aggregation-function"
                                                                )}
                                                                options={
                                                                    selectedMetric ===
                                                                    STAR_COLUMN
                                                                        ? getAggregationOptions(
                                                                              [
                                                                                  MetricAggFunction.COUNT,
                                                                              ]
                                                                          )
                                                                        : getAggregationOptions(
                                                                              [
                                                                                  MetricAggFunction.SUM,
                                                                                  MetricAggFunction.AVG,
                                                                                  MetricAggFunction.COUNT,
                                                                                  MetricAggFunction.MIN,
                                                                                  MetricAggFunction.MAX,
                                                                              ]
                                                                          )
                                                                }
                                                                subText={t(
                                                                    "message.select-aggregation-function-to-combine-multiple-data-value-into-a-single-result"
                                                                )}
                                                            />
                                                            <Button
                                                                className={
                                                                    classes.sqlButton
                                                                }
                                                                endIcon={
                                                                    showSQLWhere ? (
                                                                        <KeyboardArrowUp />
                                                                    ) : (
                                                                        <KeyboardArrowDown />
                                                                    )
                                                                }
                                                                size="small"
                                                                variant="text"
                                                                onClick={() =>
                                                                    setShowSQLWhere(
                                                                        !showSQLWhere
                                                                    )
                                                                }
                                                            >
                                                                {t(
                                                                    "label.sql-where-filter"
                                                                )}
                                                            </Button>

                                                            {showSQLWhere && (
                                                                <Grid container>
                                                                    <Grid
                                                                        item
                                                                        className={
                                                                            classes.textAreaContainer
                                                                        }
                                                                        xs={8}
                                                                    >
                                                                        <Typography
                                                                            className={
                                                                                classes.inputHeader
                                                                            }
                                                                            variant="caption"
                                                                        >
                                                                            {t(
                                                                                "label.sql-where-function"
                                                                            )}{" "}
                                                                            <Typography variant="caption">
                                                                                (
                                                                                {t(
                                                                                    "label.optional"
                                                                                )}

                                                                                )
                                                                            </Typography>
                                                                        </Typography>
                                                                        <TextareaAutosize
                                                                            aria-label="minimum height"
                                                                            className={
                                                                                classes.textArea
                                                                            }
                                                                            minRows={
                                                                                3
                                                                            }
                                                                            placeholder={t(
                                                                                "label.placeholder-sql-where"
                                                                            )}
                                                                            value={
                                                                                queryFilters
                                                                            }
                                                                            onChange={(
                                                                                e
                                                                            ) =>
                                                                                setQueryFilters(
                                                                                    e
                                                                                        .target
                                                                                        .value
                                                                                )
                                                                            }
                                                                        />
                                                                        <Box
                                                                            className={
                                                                                classes.footer
                                                                            }
                                                                        >
                                                                            <Button
                                                                                size="small"
                                                                                variant="contained"
                                                                                onClick={() =>
                                                                                    setOpenViewColumnsListDrawer(
                                                                                        true
                                                                                    )
                                                                                }
                                                                            >
                                                                                {t(
                                                                                    "label.view-columns-list"
                                                                                )}
                                                                            </Button>
                                                                        </Box>
                                                                    </Grid>
                                                                </Grid>
                                                            )}
                                                        </Grid>
                                                    ) : (
                                                        <Grid item xs={12}>
                                                            <Grid container>
                                                                <Grid
                                                                    item
                                                                    className={
                                                                        classes.textAreaContainer
                                                                    }
                                                                    xs={8}
                                                                >
                                                                    <Typography
                                                                        className={
                                                                            classes.inputHeader
                                                                        }
                                                                        variant="caption"
                                                                    >
                                                                        {t(
                                                                            "label.custom-metric"
                                                                        )}
                                                                    </Typography>
                                                                    <TextareaAutosize
                                                                        aria-label="minimum height"
                                                                        className={
                                                                            classes.textArea
                                                                        }
                                                                        minRows={
                                                                            3
                                                                        }
                                                                        value={
                                                                            editedDatasource
                                                                        }
                                                                        onChange={(
                                                                            e
                                                                        ) =>
                                                                            setEditedDatasource(
                                                                                e
                                                                                    .target
                                                                                    .value
                                                                            )
                                                                        }
                                                                    />
                                                                    <Box
                                                                        className={
                                                                            classes.footer
                                                                        }
                                                                    >
                                                                        <Button
                                                                            size="small"
                                                                            variant="contained"
                                                                            onClick={() =>
                                                                                setOpenViewColumnsListDrawer(
                                                                                    true
                                                                                )
                                                                            }
                                                                        >
                                                                            {t(
                                                                                "label.view-columns-list"
                                                                            )}
                                                                        </Button>
                                                                    </Box>
                                                                </Grid>
                                                                {/* <Grid
                                                                    item
                                                                    xs={4}
                                                                >
                                                                    <Button
                                                                        color="primary"
                                                                        startIcon={
                                                                            <InfoOutlinedIcon />
                                                                        }
                                                                        variant="outlined"
                                                                        onClick={() => {
                                                                            // TODO Add guide link
                                                                        }}
                                                                    >
                                                                        {t(
                                "label.aggregation-functions-guide-write-custom"
                                                                        )}
                                                                    </Button>
                                                                </Grid> */}
                                                            </Grid>
                                                        </Grid>
                                                    )}
                                                    <Grid item xs={12}>
                                                        <Box marginBottom="10px">
                                                            <Typography
                                                                className={
                                                                    classes.inputHeader
                                                                }
                                                                variant="h6"
                                                            >
                                                                {t(
                                                                    "label.granularity"
                                                                )}
                                                            </Typography>

                                                            <Typography variant="body2">
                                                                {t(
                                                                    "label.select-the-level-of-detail-at-which-data-is-aggregated-or-stored-in-the-time-series-data"
                                                                )}
                                                            </Typography>
                                                        </Box>
                                                        <Grid item xs={4}>
                                                            <InputSectionV2
                                                                inputComponent={
                                                                    <>
                                                                        <Autocomplete
                                                                            disableClearable
                                                                            fullWidth
                                                                            getOptionLabel={(
                                                                                option
                                                                            ) =>
                                                                                option.label
                                                                            }
                                                                            options={
                                                                                GRANULARITY_OPTIONS
                                                                            }
                                                                            renderInput={(
                                                                                params
                                                                            ) => (
                                                                                <TextField
                                                                                    {...params}
                                                                                    InputProps={{
                                                                                        ...params.InputProps,
                                                                                    }}
                                                                                    placeholder={t(
                                                                                        "label.select-granularity"
                                                                                    )}
                                                                                    variant="outlined"
                                                                                />
                                                                            )}
                                                                            renderOption={({
                                                                                value,
                                                                                label,
                                                                            }) => (
                                                                                <Box
                                                                                    alignItems="center"
                                                                                    display="flex"
                                                                                    justifyContent="space-between"
                                                                                    width="100%"
                                                                                >
                                                                                    {
                                                                                        label
                                                                                    }
                                                                                    {GRANULARITY_OPTIONS_TOOLTIP[
                                                                                        value
                                                                                    ] && (
                                                                                        <Tooltip
                                                                                            arrow
                                                                                            placement="top"
                                                                                            title={
                                                                                                GRANULARITY_OPTIONS_TOOLTIP[
                                                                                                    value
                                                                                                ]
                                                                                            }
                                                                                        >
                                                                                            <Chip
                                                                                                color="primary"
                                                                                                label={t(
                                                                                                    "label.beta"
                                                                                                )}
                                                                                                size="small"
                                                                                            />
                                                                                        </Tooltip>
                                                                                    )}
                                                                                </Box>
                                                                            )}
                                                                            value={
                                                                                granularity
                                                                                    ? {
                                                                                          value: granularity,
                                                                                          label: granularity,
                                                                                      }
                                                                                    : undefined
                                                                            }
                                                                            onChange={
                                                                                handleGranularityChange
                                                                            }
                                                                        />
                                                                    </>
                                                                }
                                                            />
                                                        </Grid>
                                                    </Grid>
                                                    <Grid item xs={12}>
                                                        <RadioSection
                                                            defaultValue={
                                                                anomalyDetection ||
                                                                undefined
                                                            }
                                                            label={t(
                                                                "label.anomalies-detection-type"
                                                            )}
                                                            options={getAnomalyDetectionOptions(
                                                                [
                                                                    AnomalyDetectionOptions.SINGLE,
                                                                    AnomalyDetectionOptions.COMPOSITE,
                                                                ]
                                                            )}
                                                            subText={t(
                                                                "message.select-the-algorithm-that-best-matches-the-data-patterns"
                                                            )}
                                                        />
                                                    </Grid>
                                                    {anomalyDetection ===
                                                        AnomalyDetectionOptions.COMPOSITE && (
                                                        <Grid item xs={12}>
                                                            <RadioSection
                                                                defaultValue={
                                                                    dimension ||
                                                                    undefined
                                                                }
                                                                label={t(
                                                                    "message.select-dimensions"
                                                                )}
                                                                options={getSelectDimensionsOptions(
                                                                    [
                                                                        SelectDimensionsOptions.ENUMERATORS,
                                                                        SelectDimensionsOptions.DIMENSION_RECOMMENDER,
                                                                    ]
                                                                )}
                                                            />
                                                        </Grid>
                                                    )}
                                                    {dimension ===
                                                        SelectDimensionsOptions.ENUMERATORS && (
                                                        <Grid item xs={12}>
                                                            <JSONEditorV2
                                                                hideValidationSuccessIcon
                                                                showFooter
                                                                actions={[
                                                                    {
                                                                        label: t(
                                                                            "label.run-enumeration"
                                                                        ),
                                                                        onClick:
                                                                            () =>
                                                                                setEnumerations(
                                                                                    true
                                                                                ),
                                                                    },
                                                                    {
                                                                        label: t(
                                                                            "label.view-columns-list"
                                                                        ),
                                                                        onClick:
                                                                            () =>
                                                                                setOpenViewColumnsListDrawer(
                                                                                    true
                                                                                ),
                                                                    },
                                                                ]}
                                                                value={
                                                                    enumerators
                                                                }
                                                                onChange={(
                                                                    value
                                                                ) =>
                                                                    setEnumerators(
                                                                        JSON.parse(
                                                                            value
                                                                        )
                                                                    )
                                                                }
                                                            />
                                                        </Grid>
                                                    )}
                                                    <Grid item xs={12}>
                                                        {((anomalyDetection ===
                                                            AnomalyDetectionOptions.COMPOSITE &&
                                                            dimension ===
                                                                SelectDimensionsOptions.DIMENSION_RECOMMENDER) ||
                                                            (anomalyDetection ===
                                                                AnomalyDetectionOptions.COMPOSITE &&
                                                                dimension ===
                                                                    SelectDimensionsOptions.ENUMERATORS &&
                                                                enumerations) ||
                                                            anomalyDetection ===
                                                                AnomalyDetectionOptions.SINGLE) &&
                                                            anomalyDetection &&
                                                            granularity &&
                                                            (aggregationFunction ||
                                                                editedDatasource) && (
                                                                <Grid
                                                                    container
                                                                    alignItems="center"
                                                                >
                                                                    {anomalyDetection !==
                                                                        AnomalyDetectionOptions.COMPOSITE ||
                                                                    compositeFilters ? (
                                                                        <Box
                                                                            className={
                                                                                classes.card
                                                                            }
                                                                        >
                                                                            <Grid
                                                                                item
                                                                                xs={
                                                                                    12
                                                                                }
                                                                            >
                                                                                {" "}
                                                                                <Grid
                                                                                    container
                                                                                    alignItems="center"
                                                                                    justifyContent="space-between"
                                                                                >
                                                                                    <Grid
                                                                                        container
                                                                                        alignItems="flex-start"
                                                                                        xs={
                                                                                            8
                                                                                        }
                                                                                    >
                                                                                        <Grid
                                                                                            item
                                                                                            xs={
                                                                                                6
                                                                                            }
                                                                                        >
                                                                                            <InputSectionV2
                                                                                                description={t(
                                                                                                    "message.for-additional-algorithms-go-to",
                                                                                                    {
                                                                                                        entity: t(
                                                                                                            "label.advanced-mode"
                                                                                                        ),
                                                                                                    }
                                                                                                )}
                                                                                                inputComponent={
                                                                                                    <Autocomplete<AvailableAlgorithmOption>
                                                                                                        fullWidth
                                                                                                        data-testId="datasource-select"
                                                                                                        getOptionLabel={(
                                                                                                            option
                                                                                                        ) =>
                                                                                                            option
                                                                                                                .algorithmOption
                                                                                                                .title as string
                                                                                                        }
                                                                                                        noOptionsText={t(
                                                                                                            "message.no-options-available-entity",
                                                                                                            {
                                                                                                                entity: t(
                                                                                                                    "label.dataset"
                                                                                                                ),
                                                                                                            }
                                                                                                        )}
                                                                                                        options={
                                                                                                            recommendedAlertTemplateFirst ||
                                                                                                            []
                                                                                                        }
                                                                                                        renderInput={(
                                                                                                            params
                                                                                                        ) => (
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
                                                                                                            option: AvailableAlgorithmOption
                                                                                                        ): JSX.Element => {
                                                                                                            return (
                                                                                                                <Box
                                                                                                                    data-testId={`${toLower(
                                                                                                                        option
                                                                                                                            .algorithmOption
                                                                                                                            .title
                                                                                                                    )}-datasource-option`}
                                                                                                                >
                                                                                                                    <Typography variant="h6">
                                                                                                                        {
                                                                                                                            option
                                                                                                                                .algorithmOption
                                                                                                                                .title
                                                                                                                        }
                                                                                                                    </Typography>
                                                                                                                </Box>
                                                                                                            );
                                                                                                        }}
                                                                                                        value={
                                                                                                            algorithmOption
                                                                                                        }
                                                                                                        onChange={(
                                                                                                            _,
                                                                                                            value
                                                                                                        ) => {
                                                                                                            if (
                                                                                                                !value
                                                                                                            ) {
                                                                                                                return;
                                                                                                            }
                                                                                                            setAlgorithmOption(
                                                                                                                value
                                                                                                            );
                                                                                                        }}
                                                                                                    />
                                                                                                }
                                                                                                label={t(
                                                                                                    "label.recommended-algorithm"
                                                                                                )}
                                                                                            />
                                                                                        </Grid>
                                                                                        <Grid
                                                                                            item
                                                                                            xs={
                                                                                                6
                                                                                            }
                                                                                        >
                                                                                            <>
                                                                                                <Typography
                                                                                                    className={
                                                                                                        classes.inputHeader
                                                                                                    }
                                                                                                    variant="caption"
                                                                                                >
                                                                                                    {t(
                                                                                                        "label.date-range"
                                                                                                    )}

                                                                                                    :
                                                                                                </Typography>
                                                                                            </>
                                                                                            <TimeRangeButtonWithContext
                                                                                                hideQuickExtend
                                                                                                btnGroupColor="default"
                                                                                                maxDate={
                                                                                                    alertInsight?.datasetEndTime
                                                                                                }
                                                                                                minDate={
                                                                                                    alertInsight?.datasetStartTime
                                                                                                }
                                                                                                timezone={determineTimezoneFromAlertInEvaluation(
                                                                                                    alertInsight?.templateWithProperties
                                                                                                )}
                                                                                                onTimeRangeChange={(
                                                                                                    newStart,
                                                                                                    newEnd
                                                                                                ) => {
                                                                                                    fetchAlertEvaluation(
                                                                                                        newStart,
                                                                                                        newEnd
                                                                                                    );
                                                                                                }}
                                                                                            />
                                                                                        </Grid>
                                                                                    </Grid>
                                                                                    <Grid>
                                                                                        <Grid
                                                                                            container
                                                                                        >
                                                                                            <Button
                                                                                                className={
                                                                                                    classes.infoButton
                                                                                                }
                                                                                                color="primary"
                                                                                                startIcon={
                                                                                                    <FilterListRoundedIcon />
                                                                                                }
                                                                                                variant="outlined"
                                                                                                onClick={() => {
                                                                                                    setShowAdvancedOptions(
                                                                                                        true
                                                                                                    );
                                                                                                }}
                                                                                            >
                                                                                                {t(
                                                                                                    "label.add-advanced-options"
                                                                                                )}
                                                                                            </Button>
                                                                                            <AdditonalFiltersDrawer
                                                                                                availableConfigurations={
                                                                                                    availableConfigurations as AnomaliesFilterConfiguratorRenderConfigs[]
                                                                                                }
                                                                                                defaultValues={
                                                                                                    alert.templateProperties
                                                                                                }
                                                                                                isOpen={
                                                                                                    showAdvancedOptions
                                                                                                }
                                                                                                onApply={
                                                                                                    handleApplyAdvancedOptions
                                                                                                }
                                                                                                onClose={() => {
                                                                                                    setShowAdvancedOptions(
                                                                                                        false
                                                                                                    );
                                                                                                }}
                                                                                            />
                                                                                        </Grid>
                                                                                    </Grid>
                                                                                </Grid>
                                                                            </Grid>
                                                                            {recommendedAlertConfigMatchingTemplate && (
                                                                                <Grid
                                                                                    item
                                                                                    xs={
                                                                                        12
                                                                                    }
                                                                                >
                                                                                    <Alert
                                                                                        action={
                                                                                            <>
                                                                                                {doesAlertHaveRecommendedValues ? (
                                                                                                    <Box
                                                                                                        alignContent="center"
                                                                                                        position="flex"
                                                                                                        style={{
                                                                                                            color: ColorV1.Green2,
                                                                                                        }}
                                                                                                        textAlign="center"
                                                                                                    >
                                                                                                        <Box
                                                                                                            mr={
                                                                                                                1
                                                                                                            }
                                                                                                        >
                                                                                                            <DoneAllIcon />
                                                                                                        </Box>
                                                                                                        <Box
                                                                                                            pr={
                                                                                                                2
                                                                                                            }
                                                                                                        >
                                                                                                            Alert
                                                                                                            Tuned
                                                                                                        </Box>
                                                                                                    </Box>
                                                                                                ) : (
                                                                                                    <Button
                                                                                                        color="primary"
                                                                                                        onClick={
                                                                                                            handleTuneAlertClick
                                                                                                        }
                                                                                                    >
                                                                                                        {t(
                                                                                                            "label.tune-my-alert"
                                                                                                        )}
                                                                                                    </Button>
                                                                                                )}
                                                                                            </>
                                                                                        }
                                                                                        severity="info"
                                                                                        style={{
                                                                                            backgroundColor:
                                                                                                "#FFF",
                                                                                        }}
                                                                                        variant="outlined"
                                                                                    >
                                                                                        <AlertTitle>
                                                                                            {t(
                                                                                                "message.we-can-tune-the-alert-for-you"
                                                                                            )}
                                                                                        </AlertTitle>
                                                                                        {t(
                                                                                            "message.our-new-feature-sets-up-your-alert-with-the-parameters"
                                                                                        )}
                                                                                    </Alert>
                                                                                </Grid>
                                                                            )}

                                                                            {algorithmOption && (
                                                                                <Grid
                                                                                    item
                                                                                    xs={
                                                                                        12
                                                                                    }
                                                                                >
                                                                                    <ThresholdSetup
                                                                                        alert={
                                                                                            alert
                                                                                        }
                                                                                        alertTemplate={
                                                                                            selectedAlertTemplate
                                                                                        }
                                                                                        algorithmOptionConfig={
                                                                                            algorithmOption
                                                                                        }
                                                                                        onAlertPropertyChange={
                                                                                            onAlertPropertyChange
                                                                                        }
                                                                                    >
                                                                                        {anomalyDetection ===
                                                                                            AnomalyDetectionOptions.COMPOSITE &&
                                                                                            compositeFilters && (
                                                                                                <Button
                                                                                                    color="primary"
                                                                                                    onClick={() =>
                                                                                                        setOpenCompositeFilterModal(
                                                                                                            true
                                                                                                        )
                                                                                                    }
                                                                                                >
                                                                                                    {t(
                                                                                                        "label.add-dimensions"
                                                                                                    )}
                                                                                                </Button>
                                                                                            )}
                                                                                    </ThresholdSetup>
                                                                                </Grid>
                                                                            )}
                                                                        </Box>
                                                                    ) : (
                                                                        <Box
                                                                            className={
                                                                                classes.card
                                                                            }
                                                                            display="flex"
                                                                            justifyContent="center"
                                                                        >
                                                                            <Grid>
                                                                                <Box marginBottom="10px">
                                                                                    <Typography variant="h5">
                                                                                        {t(
                                                                                            "label.dimensions-recommender"
                                                                                        )}
                                                                                    </Typography>

                                                                                    <Typography variant="body2">
                                                                                        {t(
                                                                                            "message.find-top-dimension-contributors-to-create-the-alert"
                                                                                        )}
                                                                                    </Typography>
                                                                                </Box>
                                                                                <Button
                                                                                    color="primary"
                                                                                    startIcon={
                                                                                        <AddCircleOutline />
                                                                                    }
                                                                                    variant="outlined"
                                                                                    onClick={() =>
                                                                                        setOpenCompositeFilterModal(
                                                                                            true
                                                                                        )
                                                                                    }
                                                                                >
                                                                                    {t(
                                                                                        "label.add-dimensions"
                                                                                    )}
                                                                                </Button>
                                                                            </Grid>
                                                                            <Grid>
                                                                                <img
                                                                                    src={
                                                                                        DimensionImage
                                                                                    }
                                                                                />
                                                                            </Grid>
                                                                        </Box>
                                                                    )}
                                                                </Grid>
                                                            )}

                                                        <Grid item xs={12}>
                                                            <Box
                                                                display="flex"
                                                                gridGap={10}
                                                                paddingTop={2}
                                                            >
                                                                <Button
                                                                    color="primary"
                                                                    variant="outlined"
                                                                    onClick={() => {
                                                                        navigate(
                                                                            getAlertsAllPath()
                                                                        );
                                                                    }}
                                                                >
                                                                    {t(
                                                                        "label.cancel"
                                                                    )}
                                                                </Button>
                                                                <Button
                                                                    color="primary"
                                                                    disabled={
                                                                        isCreateButtonDisabled
                                                                    }
                                                                    onClick={() =>
                                                                        setOpenCreateAlertModal(
                                                                            true
                                                                        )
                                                                    }
                                                                >
                                                                    {t(
                                                                        alert.id
                                                                            ? "label.update-alert"
                                                                            : "label.create-alert"
                                                                    )}
                                                                </Button>
                                                            </Box>
                                                        </Grid>
                                                    </Grid>
                                                </Grid>
                                            </Grid>
                                        )}
                                    </Grid>
                                </Box>
                            </Grid>
                        </Grid>
                        {openCompositeFilterModal && (
                            <AlertCompositeFiltersModal
                                onCancel={() =>
                                    setOpenCompositeFilterModal(false)
                                }
                                onUpdateCompositeFiltersChange={
                                    onUpdateCompositeFiltersChange
                                }
                            />
                        )}
                        {openCreateAlertModal && (
                            <CreateAlertModal
                                onCancel={() => setOpenCreateAlertModal(false)}
                            />
                        )}
                        {!isNil(selectedTable?.dataset?.id) && (
                            <ColumnsDrawer
                                datasetId={selectedTable?.dataset.id}
                                isOpen={openViewColumnsListDrawer}
                                onClose={() =>
                                    setOpenViewColumnsListDrawer(
                                        (prev) => !prev
                                    )
                                }
                            />
                        )}
                    </PageContentsCardV1>
                </Grid>
            </ThemeProvider>
        </>
    );
};
