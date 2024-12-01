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
    CircularProgress,
    Divider,
    Grid,
    Switch,
    TextareaAutosize,
    TextField,
    Typography,
} from "@material-ui/core";
import {
    Cancel,
    CheckCircle,
    KeyboardArrowDown,
    KeyboardArrowUp,
} from "@material-ui/icons";
import AddCircleOutline from "@material-ui/icons/AddCircleOutline";
import { Alert, Autocomplete } from "@material-ui/lab";
import { isNil, toLower } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import DimensionImage from "../../../../assets/images/dimensions.png";
import { AdditonalFiltersDrawer } from "../../../components/additional-filters-drawer/additional-filters-drawer.component";
import { AlertCompositeFiltersModal } from "../../../components/alert-composite-filters-modal/alert-composite-filters-modal.component";
import { AlertJsonEditorModal } from "../../../components/alert-json-editor-modal/alert-json-editor-modal.component";
// Remove "createNewStartingAlertThreshold as" for fallback
import { createNewStartingAlertThreshold as createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { AvailableAlgorithmOption } from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.interfaces";
import {
    generateAvailableAlgorithmOptions,
    generateAvailableAlgorithmOptionsForRecommendations,
} from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.utils";
import { AnomaliesFilterConfiguratorRenderConfigs } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.interfaces";
import { getAvailableFilterOptions } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.utils";
import { NotificationConfiguration } from "../../../components/alert-wizard-v3/notification-configuration/notification-configuration.component";
import { ChartContentV2 } from "../../../components/alert-wizard-v3/preview-chart/chart-content-v2/chart-content-v2.component";
import {
    generateTemplateProperties,
    GranularityValue,
} from "../../../components/alert-wizard-v3/select-metric/select-metric.utils";
import { ThresholdSetupV3 } from "../../../components/alert-wizard-v3/threshold-setup/threshold-setup-v3.component";
import { ColumnsDrawer } from "../../../components/columns-drawer/columns-drawer.component";
import { CreateAlertModal } from "../../../components/create-alert-modal/create-alert-modal.component";
import { InputSectionV2 } from "../../../components/form-basics/input-section-v2/input-section-v2.component";
import { RadioSection } from "../../../components/form-basics/radio-section-v2/radio-section.component";
import { RadioSectionOptions } from "../../../components/form-basics/radio-section-v2/radio-section.interfaces";
import { TimeRangeButtonWithContext } from "../../../components/time-range/time-range-button-with-context-v2/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { ReactComponent as FilterListRoundedIcon } from "../../../platform/assets/images/filter-icon.svg";
import {
    PageContentsCardV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
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
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { getAlertsAllPath } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../../alerts-create-guided-page/alerts-create-guided-page.interfaces";
import { SETUP_DETAILS_TEST_IDS } from "../../alerts-create-guided-page/setup-details/setup-details-page.interface";
import { easyAlertStyles } from "./alerts-create-easy-page.styles";

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
const ALERT_TEMPLATE_FOR_EVALUATE_QUERY_DX = "startree-threshold-query-dx";

export const AlertsCreateEasyPage: FunctionComponent = () => {
    const classes = easyAlertStyles();

    const { t } = useTranslation();
    const navigate = useNavigate();

    const [searchParams, setSearchParams] = useSearchParams();
    const [showAdvancedOptions, setShowAdvancedOptions] = useState(false);
    const [queryFilters, setQueryFilters] = useState("");
    const [inputValue, setInputValue] = useState("");

    const [showSQLWhere, setShowSQLWhere] = useState(false);
    const [enumerations, setEnumerations] = useState(false);
    const [dimension, setDimension] = useState<string | null>(null);
    const [alertInsightLoading, setAlertInsightLoading] = useState(false);
    const [isNotificationsOn, setIsNotificationsOn] = useState(false);
    const { notify } = useNotificationProviderV1();

    const GRANULARITY_OPTIONS = [
        {
            label: t("label.daily"),
            value: GranularityValue.DAILY,
        },
        {
            label: t("label.hourly"),
            value: GranularityValue.HOURLY,
        },
        {
            label: t("label.15-minutes"),
            value: GranularityValue.FIFTEEN_MINUTES,
        },
        {
            label: t("label.5-minutes"),
            value: GranularityValue.FIVE_MINUTES,
        },
        {
            label: t("label.1-minute"),
            value: GranularityValue.ONE_MINUTE,
        },
    ];

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
        getAlertInsight,
        alertRecommendations,
        getAlertRecommendationIsLoading,
        alert,
        setIsMultiDimensionAlert,
        getAlertRecommendation,
        setShouldShowStepper,
        selectedSubscriptionGroups,
        handleSubscriptionGroupChange,
        newSubscriptionGroup,
        onNewSubscriptionGroupChange,
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
    const [editedDatasourceFieldValue, setEditedDatasourceFieldValue] =
        useState("");

    const [enumerators, setEnumerators] = useState("");
    const [algorithmOption, setAlgorithmOption] =
        useState<AvailableAlgorithmOption | null>(null);

    const [compositeFilters, setCompositeFilters] =
        useState<TemplatePropertiesObject | null>(null);
    const [openCompositeFilterModal, setOpenCompositeFilterModal] =
        useState(false);
    const [openCreateAlertModal, setOpenCreateAlertModal] = useState(false);
    const [openViewColumnsListDrawer, setOpenViewColumnsListDrawer] =
        useState(false);

    const isGetAlertRecommendationLoading = useMemo(() => {
        return getAlertRecommendationIsLoading;
    }, [getAlertRecommendationIsLoading]);

    useEffect(() => {
        setShouldShowStepper(false);
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
                setEditedDatasourceFieldValue(
                    String(alert.templateProperties?.aggregationColumn)
                );
                setEditedDatasourceFieldValue(
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
                    alertTemplateOptions.find((item) => {
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
                if (alert.templateProperties?.enumeratorQuery) {
                    setEnumerators(
                        String(alert.templateProperties?.enumeratorQuery)
                    );
                    setDimension(SelectDimensionsOptions.ENUMERATORS);
                }
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
        let alertTemplateToFind = isMultiDimensionAlert
            ? algorithmOption?.algorithmOption.alertTemplateForMultidimension
            : algorithmOption?.algorithmOption.alertTemplate;
        if (!algorithmOption) {
            alertTemplateToFind = isMultiDimensionAlert
                ? ALERT_TEMPLATE_FOR_EVALUATE_DX
                : ALERT_TEMPLATE_FOR_EVALUATE;
        }

        return alertTemplates.find((alertTemplateCandidate) => {
            return alertTemplateCandidate.name === alertTemplateToFind;
        });
    }, [alertTemplates, alert, algorithmOption, isMultiDimensionAlert]);

    const availableConfigurations = useMemo(() => {
        if (!alertTemplateForEvaluate) {
            return undefined;
        }

        return getAvailableFilterOptions(alertTemplateForEvaluate, t);
    }, [alertTemplateForEvaluate]);

    const handleGranularityChange = async (
        _: unknown,
        item: { label: string; value: GranularityValue } | null
    ): Promise<void> => {
        const existingGranularity = granularity;
        if (!item) {
            if (aggregationFunction) {
                handleAggregationChange(aggregationFunction);
            }

            return;
        }
        setGranularity(item.value);
        setDimension(null);
        setCompositeFilters(null);
        setAlgorithmOption(null);
        setEnumerations(false);
        setEnumerators("");
        setAnomalyDetection(null);
        if (
            item.value &&
            (editedDatasourceFieldValue || selectedMetric) &&
            selectedTable
        ) {
            try {
                let isCustomMetrics = false;
                if (selectedMetric === t("label.custom-metric-aggregation")) {
                    isCustomMetrics = true;
                }
                const workingAlert: Partial<EditableAlert> = existingGranularity
                    ? {
                          ...alert,
                          templateProperties: {
                              ...alert.templateProperties,
                              monitoringGranularity: item.value,
                          },
                      }
                    : {
                          template: {
                              name:
                                  (isMultiDimensionAlert
                                      ? algorithmOption?.algorithmOption
                                            .alertTemplateForMultidimension
                                      : algorithmOption?.algorithmOption
                                            ?.alertTemplate) ||
                                  createNewStartingAlert().template?.name,
                          },
                          templateProperties: {
                              ...createNewStartingAlert().templateProperties,
                              ...generateTemplateProperties(
                                  isCustomMetrics
                                      ? editedDatasourceFieldValue
                                      : (selectedMetric as string),
                                  selectedTable?.dataset,
                                  aggregationFunction || "",
                                  item.value
                              ),
                              queryFilters: queryFilters,
                              enumeratorQuery:
                                  dimension ===
                                  SelectDimensionsOptions.ENUMERATORS
                                      ? enumerators
                                      : null,
                          },
                      };
                setAlertInsightLoading(true);
                const newAlertInsight = await getAlertInsight({
                    alert: workingAlert as EditableAlert,
                });
                if (newAlertInsight) {
                    searchParams.set(
                        TimeRangeQueryStringKey.START_TIME,
                        newAlertInsight.defaultStartTime.toString()
                    );
                    searchParams.set(
                        TimeRangeQueryStringKey.END_TIME,
                        newAlertInsight.defaultEndTime.toString()
                    );
                    setSearchParams(searchParams);
                }
            } catch (error) {
                console.error("Error fetching alert insight:", error);
            } finally {
                setAlertInsightLoading(false);
            }
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
                onClick: () => handleAggregationChange(item),
            })
        );

        return options;
    };

    const handleAnomalyDetectionChange = (item: string): void => {
        setEnumerations(false);
        setEnumerators("");
        setDimension(null);
        setAnomalyDetection(item);
        setAlgorithmOption(null);
        let isCustomMetrics = false;
        if (selectedMetric === t("label.custom-metric-aggregation")) {
            isCustomMetrics = true;
        }
        if (
            !selectedTable ||
            !selectedMetric ||
            !(
                aggregationFunction ||
                (isCustomMetrics && editedDatasourceFieldValue)
            ) ||
            !granularity
        ) {
            return;
        }
        if (item === AnomalyDetectionOptions.SINGLE) {
            const workingAlert = {
                template: {
                    name: createNewStartingAlert().template?.name,
                },
                templateProperties: {
                    ...createNewStartingAlert().templateProperties,
                    ...generateTemplateProperties(
                        isCustomMetrics
                            ? editedDatasourceFieldValue
                            : (selectedMetric as string),
                        selectedTable?.dataset,
                        aggregationFunction || "",
                        granularity
                    ),
                    queryFilters: queryFilters ? queryFilters : "",
                },
            };
            onAlertPropertyChange(workingAlert);
            getAlertRecommendation({ ...alert, ...workingAlert });
            handleReloadPreviewClick(workingAlert);
        } else if (item === AnomalyDetectionOptions.COMPOSITE) {
            setCompositeFilters(null);
            if (queryFilters) {
                const updatedQueryFilters = queryFilters
                    .trim()
                    .startsWith("${queryFilters}")
                    ? queryFilters
                    : "${queryFilters} " + queryFilters;
                setQueryFilters(updatedQueryFilters);
            }
            setIsMultiDimensionAlert(true);
        }
    };

    const handleRunEnumerations = (): void => {
        if (
            !selectedTable ||
            !selectedMetric ||
            !aggregationFunction ||
            !granularity
        ) {
            return;
        }
        setEnumerations(true);
        setAlgorithmOption(null);
        setAlgorithmOption(null);
        const workingAlert = {
            template: {
                name: ALERT_TEMPLATE_FOR_EVALUATE_QUERY_DX,
            },
            templateProperties: {
                ...generateTemplateProperties(
                    selectedMetric as string,
                    selectedTable?.dataset,
                    aggregationFunction || "",
                    granularity
                ),
                min: 0,
                max: 1,
                queryFilters: queryFilters ? queryFilters : "${queryFilters}",
                enumeratorQuery: enumerators,
            },
        };
        onAlertPropertyChange(workingAlert);
        getAlertRecommendation({ ...alert, ...workingAlert });
        handleReloadPreviewClick(workingAlert);
    };

    const getAnomalyDetectionOptions = (
        values: Array<string>
    ): RadioSectionOptions[] => {
        const options: RadioSectionOptions[] = [];
        values.map((item) =>
            options.push({
                value: item,
                label: item,
                disabled: alertInsightLoading,
                onClick: () => handleAnomalyDetectionChange(item),
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
                onClick: () => {
                    setDimension(() => {
                        setEnumerators("");
                        setEnumerations(false);
                        setCompositeFilters(null);
                        setAlgorithmOption(null);
                        if (
                            selectedTable?.dataset &&
                            selectedMetric &&
                            aggregationFunction &&
                            granularity
                        ) {
                            const workingAlert = {
                                template: {
                                    name: ALERT_TEMPLATE_FOR_EVALUATE_QUERY_DX,
                                },
                                templateProperties: {
                                    ...createNewStartingAlert()
                                        .templateProperties,
                                    ...generateTemplateProperties(
                                        selectedMetric as string,
                                        selectedTable?.dataset,
                                        aggregationFunction || "",
                                        granularity
                                    ),
                                },
                            };
                            onAlertPropertyChange(workingAlert);
                        }

                        return item;
                    });
                },
                tooltipText: item,
            })
        );

        return options;
    };

    const alertTemplateOptions = useMemo(() => {
        const availableAlgorithmOptions = generateAvailableAlgorithmOptions(
            alertTemplates.map((a: AlertTemplate) => a.name)
        ).filter((option) =>
            isMultiDimensionAlert
                ? option.hasMultidimension
                : option.hasAlertTemplate
        );
        const recommendedAlgorithmOptions =
            generateAvailableAlgorithmOptionsForRecommendations(
                alertRecommendations ?? [],
                isMultiDimensionAlert
            );

        return [...recommendedAlgorithmOptions, ...availableAlgorithmOptions];
    }, [alertTemplates, alertRecommendations]);

    const {
        getEvaluation,
        evaluation,
        status: AlertEvaluationStatus,
        errorMessages,
    } = useGetEvaluation();

    useEffect(() => {
        notifyIfErrors(
            AlertEvaluationStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.subscription-groups"),
            })
        );
    }, [AlertEvaluationStatus]);

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

    const fetchAlertEvaluation = (
        start: number,
        end: number,
        alert?: Partial<EditableAlert>
    ): void => {
        const copiedAlert = { ...alertConfigForPreview, ...(alert ?? {}) };
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
            (!aggregationFunction &&
                !(isCustomMetrics && editedDatasourceFieldValue))
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
                    isCustomMetrics
                        ? editedDatasourceFieldValue
                        : (selectedMetric as string),
                    selectedTable?.dataset,
                    aggregationFunction || "",
                    granularity
                ),
                queryFilters: queryFilters,
                enumeratorQuery:
                    dimension === SelectDimensionsOptions.ENUMERATORS
                        ? enumerators
                        : null,
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
        enumerators,
    ]);

    const handleDatasetChange = (dataset: DatasetInfo): void => {
        setSelectedTable(dataset);
        setSelectedMetric(null);
        setAggregationFunction(null);
        setGranularity(null);
        setQueryFilters("");
        setInputValue("");
        setAlgorithmOption(null);
        setCompositeFilters(null);
        setAnomalyDetection(null);
        setEnumerations(false);
        setEnumerators("");
        setDimension(null);
    };

    const handleMetricChange = (metric: string): void => {
        setSelectedMetric(metric);
        setAggregationFunction(null);
        setGranularity(null);
        setAlgorithmOption(null);
        setCompositeFilters(null);
        setQueryFilters("");
        setInputValue("");
        setAnomalyDetection(null);
        setEditedDatasourceFieldValue("");
        setEnumerations(false);
        setEnumerators("");
        setDimension(null);
    };

    const handleAggregationChange = (aggregation: string): void => {
        setAggregationFunction(aggregation);
        setGranularity(null);
        setAlgorithmOption(null);
        setCompositeFilters(null);
        setAnomalyDetection(null);
        setEnumerations(false);
        setQueryFilters("");
        setInputValue("");
        setEnumerators("");
        setDimension(null);
    };

    const handleSqlChange = (sql: string): void => {
        setQueryFilters(sql);
        setGranularity(null);
        setAlgorithmOption(null);
        setCompositeFilters(null);
        setAnomalyDetection(null);
        setEnumerations(false);
        setInputValue("");
        setEnumerators("");
        setDimension(null);
    };

    const handleReloadPreviewClick = (alert?: Partial<EditableAlert>): void => {
        if ((!startTime || !endTime) && alertInsight) {
            // If start or end is missing and there exists an alert insight
            fetchAlertEvaluation(
                alertInsight.defaultStartTime,
                alertInsight.defaultEndTime,
                alert
            );
        } else {
            fetchAlertEvaluation(startTime, endTime, alert);
        }
    };

    const selectedAlertTemplate = useMemo(() => {
        return alertTemplates.find((alertTemplateCandidate) => {
            return alertTemplateCandidate.name === alert.template?.name;
        });
    }, [alertTemplates, alert]);

    const onUpdateCompositeFiltersChange = (
        template: TemplatePropertiesObject
    ): void => {
        if (
            !selectedTable ||
            !selectedMetric ||
            !aggregationFunction ||
            !granularity
        ) {
            return;
        }
        setEnumerations(false);
        setEnumerators("");
        const workingAlert = {
            template: {
                name: ALERT_TEMPLATE_FOR_EVALUATE_DX,
            },
            templateProperties: {
                ...template,
                queryFilters: queryFilters ? queryFilters : "${queryFilters}",
            },
        };
        onAlertPropertyChange(workingAlert);
        getAlertRecommendation({ ...alert, ...workingAlert });
        handleReloadPreviewClick(workingAlert);
        setCompositeFilters(template);
    };

    const handleAlgorithmChange = (
        algorithm: AvailableAlgorithmOption
    ): void => {
        if (
            !algorithm ||
            !selectedTable ||
            !selectedMetric ||
            !aggregationFunction ||
            !granularity
        ) {
            return;
        }
        setAlgorithmOption(algorithm);
        const isCompositeAlert =
            anomalyDetection === AnomalyDetectionOptions.COMPOSITE;
        const isRecommendation =
            algorithm?.recommendationLabel ===
            t("label.recommended-configuration");
        const recommendedTemplate = isRecommendation
            ? alertRecommendations.find(
                  (rec, index) =>
                      `${rec.alert.template?.name}-${index}` ===
                      algorithm?.recommendationId
              )
            : null;
        const isEnumeratorQuery =
            dimension === SelectDimensionsOptions.ENUMERATORS;
        const defaultAlertTemplate =
            createNewStartingAlert().templateProperties;
        delete (defaultAlertTemplate as TemplatePropertiesObject).min;
        delete (defaultAlertTemplate as TemplatePropertiesObject).max;
        const workingAlert = {
            template: {
                name: isCompositeAlert
                    ? isEnumeratorQuery
                        ? algorithm?.algorithmOption?.alertTemplateForMultidimension?.replace(
                              "-dx",
                              "-query-dx"
                          )
                        : algorithm?.algorithmOption
                              ?.alertTemplateForMultidimension
                    : algorithm?.algorithmOption?.alertTemplate,
            },
            templateProperties: recommendedTemplate?.alert.templateProperties
                ? {
                      ...recommendedTemplate.alert.templateProperties,
                  }
                : {
                      ...(isEnumeratorQuery ? {} : defaultAlertTemplate),
                      ...generateTemplateProperties(
                          selectedMetric as string,
                          selectedTable?.dataset,
                          aggregationFunction || "",
                          granularity
                      ),
                      ...(isCompositeAlert ? { ...compositeFilters } : {}),
                      queryFilters: queryFilters
                          ? queryFilters
                          : isCompositeAlert
                          ? "${queryFilters}"
                          : "",
                      ...(isEnumeratorQuery
                          ? { enumeratorQuery: enumerators }
                          : {}),
                      ...(isEnumeratorQuery
                          ? {
                                min: 0,
                                max: 1,
                            }
                          : {}),
                  },
        };
        onAlertPropertyChange(workingAlert);
    };

    const NotificationView = (): JSX.Element => {
        return (
            <Grid item xs={12}>
                <PageContentsCardV1 className={classes.notificationContainer}>
                    <Grid container>
                        <Grid item lg={3} md={5} sm={10} xs={10}>
                            <Box marginBottom={2}>
                                <Typography variant="h5">
                                    {t("label.configure-notifications")}
                                </Typography>
                                <Typography variant="body2">
                                    {t(
                                        "message.select-who-to-notify-when-finding-anomalies"
                                    )}
                                </Typography>
                            </Box>
                        </Grid>
                        <Grid item lg={9} md={7} sm={2} xs={2}>
                            <Switch
                                checked={isNotificationsOn}
                                color="primary"
                                data-testid={
                                    SETUP_DETAILS_TEST_IDS.CONFIGURATION_SWITCH
                                }
                                name="checked"
                                onChange={() =>
                                    setIsNotificationsOn(!isNotificationsOn)
                                }
                            />
                        </Grid>

                        {isNotificationsOn && (
                            <NotificationConfiguration
                                alert={alert}
                                initiallySelectedSubscriptionGroups={
                                    selectedSubscriptionGroups
                                }
                                newSubscriptionGroup={newSubscriptionGroup}
                                onNewSubscriptionGroupChange={
                                    onNewSubscriptionGroupChange
                                }
                                onSubscriptionGroupsChange={
                                    handleSubscriptionGroupChange
                                }
                            />
                        )}
                    </Grid>
                </PageContentsCardV1>
            </Grid>
        );
    };

    return (
        <>
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
                                        </Box>
                                        <Box>
                                            <Typography variant="body2">
                                                {t("message.lets-get-started")}
                                            </Typography>
                                        </Box>
                                    </Grid>

                                    <Grid item xs={12}>
                                        <Grid container>
                                            <Grid item xs={4}>
                                                <InputSectionV2
                                                    description={t(
                                                        "message.select-a-dataset-to-monitor"
                                                    )}
                                                    inputComponent={
                                                        <Autocomplete<DatasetInfo>
                                                            fullWidth
                                                            data-testId="datasource-select"
                                                            getOptionLabel={(
                                                                option
                                                            ) =>
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
                                                                        "message.select-a-dataset"
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
                                                                handleDatasetChange(
                                                                    selectedTableInfo
                                                                );
                                                            }}
                                                        />
                                                    }
                                                    label={t("label.dataset")}
                                                />
                                            </Grid>
                                            <Grid item xs={4}>
                                                <InputSectionV2
                                                    description={t(
                                                        "message.select-a-metric-to-detect-anomalies"
                                                    )}
                                                    inputComponent={
                                                        <Autocomplete<string>
                                                            fullWidth
                                                            data-testId="metric-select"
                                                            disabled={
                                                                !selectedTable
                                                            }
                                                            groupBy={(option) =>
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
                                                                                  "message.select-a-metric"
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
                                                                    handleMetricChange(
                                                                        metric
                                                                    );
                                                            }}
                                                        />
                                                    }
                                                    label={t("label.metric")}
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
                                                            value={
                                                                aggregationFunction ||
                                                                undefined
                                                            }
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
                                                                        value={queryFilters.replace(
                                                                            /\$\{queryFilters\}/g,
                                                                            ""
                                                                        )}
                                                                        onChange={(
                                                                            e
                                                                        ) =>
                                                                            handleSqlChange(
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
                                                                        justifyContent="end"
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
                                                                    minRows={3}
                                                                    value={
                                                                        editedDatasourceFieldValue
                                                                    }
                                                                    onChange={(
                                                                        e
                                                                    ) =>
                                                                        setEditedDatasourceFieldValue(
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
                                                                    justifyContent="flex-end"
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
                                                                "label.select-the-time-increment-that-the-data-is-aggregated-to"
                                                            )}
                                                        </Typography>
                                                    </Box>
                                                    <Grid item xs={4}>
                                                        <InputSectionV2
                                                            inputComponent={
                                                                <>
                                                                    <Autocomplete
                                                                        fullWidth
                                                                        getOptionLabel={(
                                                                            option
                                                                        ) =>
                                                                            option.label
                                                                        }
                                                                        getOptionSelected={(
                                                                            option,
                                                                            value
                                                                        ) =>
                                                                            option.value ===
                                                                            value.value
                                                                        }
                                                                        inputValue={
                                                                            inputValue
                                                                        }
                                                                        options={
                                                                            GRANULARITY_OPTIONS
                                                                        }
                                                                        renderInput={(
                                                                            params
                                                                        ) => {
                                                                            return (
                                                                                <TextField
                                                                                    {...params}
                                                                                    placeholder={t(
                                                                                        "label.select-granularity"
                                                                                    )}
                                                                                    variant="outlined"
                                                                                />
                                                                            );
                                                                        }}
                                                                        renderOption={({
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
                                                                            </Box>
                                                                        )}
                                                                        value={
                                                                            granularity
                                                                                ? GRANULARITY_OPTIONS.find(
                                                                                      (
                                                                                          g
                                                                                      ) =>
                                                                                          g.value ===
                                                                                          granularity
                                                                                  )
                                                                                : undefined
                                                                        }
                                                                        onChange={
                                                                            handleGranularityChange
                                                                        }
                                                                        onInputChange={(
                                                                            _event,
                                                                            newInputValue
                                                                        ) => {
                                                                            setInputValue(
                                                                                newInputValue
                                                                            );
                                                                        }}
                                                                    />
                                                                </>
                                                            }
                                                        />
                                                    </Grid>
                                                </Grid>
                                                <Grid item xs={12}>
                                                    <RadioSection
                                                        label={t(
                                                            "label.detection-type"
                                                        )}
                                                        options={getAnomalyDetectionOptions(
                                                            [
                                                                AnomalyDetectionOptions.SINGLE,
                                                                AnomalyDetectionOptions.COMPOSITE,
                                                            ]
                                                        )}
                                                        value={
                                                            anomalyDetection ||
                                                            undefined
                                                        }
                                                    />
                                                    {alertInsightLoading && (
                                                        <Box
                                                            alignItems="center"
                                                            display="flex"
                                                        >
                                                            <CircularProgress
                                                                color="primary"
                                                                size={12}
                                                            />
                                                            <Typography
                                                                style={{
                                                                    marginLeft:
                                                                        "4px",
                                                                }}
                                                                variant="caption"
                                                            >
                                                                {t(
                                                                    "label.loading-insights"
                                                                )}
                                                            </Typography>
                                                        </Box>
                                                    )}
                                                </Grid>
                                                {anomalyDetection ===
                                                    AnomalyDetectionOptions.COMPOSITE && (
                                                    <Grid item xs={12}>
                                                        <RadioSection
                                                            label={t(
                                                                "message.select-dimensions"
                                                            )}
                                                            options={getSelectDimensionsOptions(
                                                                [
                                                                    SelectDimensionsOptions.ENUMERATORS,
                                                                    SelectDimensionsOptions.DIMENSION_RECOMMENDER,
                                                                ]
                                                            )}
                                                            value={dimension}
                                                        />
                                                    </Grid>
                                                )}
                                                {dimension ===
                                                    SelectDimensionsOptions.ENUMERATORS && (
                                                    <>
                                                        <Grid item xs={12}>
                                                            <Grid item xs={12}>
                                                                <Grid container>
                                                                    <Grid
                                                                        item
                                                                        className={
                                                                            classes.textAreaContainer
                                                                        }
                                                                        xs={12}
                                                                    >
                                                                        <TextareaAutosize
                                                                            aria-label="minimum height"
                                                                            className={
                                                                                classes.textArea
                                                                            }
                                                                            minRows={
                                                                                3
                                                                            }
                                                                            placeholder={t(
                                                                                "label.select-distinct-dimension-from-dataset",
                                                                                {
                                                                                    dimension:
                                                                                        selectedTable
                                                                                            ?.dimensions?.[0] ??
                                                                                        "someColumn",
                                                                                    dataset:
                                                                                        selectedTable
                                                                                            ?.dataset
                                                                                            ?.name,
                                                                                }
                                                                            )}
                                                                            value={
                                                                                enumerators
                                                                            }
                                                                            onChange={(
                                                                                e
                                                                            ) =>
                                                                                setEnumerators(
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
                                                                            justifyContent="space-between"
                                                                        >
                                                                            <Button
                                                                                size="small"
                                                                                variant="contained"
                                                                                onClick={() =>
                                                                                    handleRunEnumerations()
                                                                                }
                                                                            >
                                                                                {t(
                                                                                    "label.run-enumeration"
                                                                                )}
                                                                            </Button>
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
                                                            </Grid>
                                                        </Grid>
                                                    </>
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
                                                            editedDatasourceFieldValue) && (
                                                            <Grid
                                                                container
                                                                alignItems="center"
                                                            >
                                                                {dimension ===
                                                                    SelectDimensionsOptions.ENUMERATORS ||
                                                                anomalyDetection !==
                                                                    AnomalyDetectionOptions.COMPOSITE ||
                                                                compositeFilters ? (
                                                                    <Box
                                                                        className={
                                                                            classes.card
                                                                        }
                                                                    >
                                                                        <Grid
                                                                            item
                                                                            className={
                                                                                classes.algorithmContainer
                                                                            }
                                                                            xs={
                                                                                12
                                                                            }
                                                                        >
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
                                                                                            inputComponent={
                                                                                                <Autocomplete<AvailableAlgorithmOption>
                                                                                                    fullWidth
                                                                                                    className={
                                                                                                        isGetAlertRecommendationLoading
                                                                                                            ? classes.animatedBorder
                                                                                                            : ""
                                                                                                    }
                                                                                                    data-testId="datasource-select"
                                                                                                    getOptionLabel={(
                                                                                                        option
                                                                                                    ) =>
                                                                                                        option
                                                                                                            .algorithmOption
                                                                                                            .title as string
                                                                                                    }
                                                                                                    groupBy={(
                                                                                                        option
                                                                                                    ) =>
                                                                                                        option.recommendationLabel ||
                                                                                                        ""
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
                                                                                                        alertTemplateOptions ||
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
                                                                                                                "message.select-algorithm"
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
                                                                                                        handleAlgorithmChange(
                                                                                                            value
                                                                                                        );
                                                                                                    }}
                                                                                                />
                                                                                            }
                                                                                            labelComponent={
                                                                                                <Box
                                                                                                    className={
                                                                                                        classes.recommendedAlgorithmContainer
                                                                                                    }
                                                                                                >
                                                                                                    <Typography
                                                                                                        className={
                                                                                                            classes.recommendedAlgorithmText
                                                                                                        }
                                                                                                        variant="caption"
                                                                                                    >
                                                                                                        {t(
                                                                                                            "label.detection-algorithm"
                                                                                                        )}
                                                                                                    </Typography>
                                                                                                    <Box
                                                                                                        className={
                                                                                                            classes.detectionRecommendationsContainer
                                                                                                        }
                                                                                                    >
                                                                                                        {isGetAlertRecommendationLoading ? (
                                                                                                            <Box display="flex">
                                                                                                                <CircularProgress
                                                                                                                    color="primary"
                                                                                                                    size={
                                                                                                                        15
                                                                                                                    }
                                                                                                                />

                                                                                                                <Typography
                                                                                                                    style={{
                                                                                                                        marginLeft:
                                                                                                                            "4px",
                                                                                                                    }}
                                                                                                                    variant="caption"
                                                                                                                >
                                                                                                                    {t(
                                                                                                                        "label.computing-detection-recommendations"
                                                                                                                    )}
                                                                                                                </Typography>
                                                                                                            </Box>
                                                                                                        ) : alertRecommendations?.length >
                                                                                                          0 ? (
                                                                                                            <Box
                                                                                                                alignItems="center"
                                                                                                                display="flex"
                                                                                                            >
                                                                                                                <CheckCircle
                                                                                                                    className={
                                                                                                                        classes.checkCircleIcon
                                                                                                                    }
                                                                                                                />
                                                                                                                <Typography
                                                                                                                    className={
                                                                                                                        classes.detectionRecommendationsReadyText
                                                                                                                    }
                                                                                                                    variant="caption"
                                                                                                                >
                                                                                                                    {t(
                                                                                                                        "label.detection-recommendations-ready"
                                                                                                                    )}
                                                                                                                </Typography>
                                                                                                            </Box>
                                                                                                        ) : (
                                                                                                            <Box
                                                                                                                alignItems="center"
                                                                                                                display="flex"
                                                                                                            >
                                                                                                                <Cancel
                                                                                                                    className={
                                                                                                                        classes.cancelIcon
                                                                                                                    }
                                                                                                                />
                                                                                                                <Typography
                                                                                                                    className={
                                                                                                                        classes.detectionRecommendationsFailedText
                                                                                                                    }
                                                                                                                    variant="caption"
                                                                                                                >
                                                                                                                    {t(
                                                                                                                        "errors.could-not-compute-detection-recommendations"
                                                                                                                    )}
                                                                                                                </Typography>
                                                                                                            </Box>
                                                                                                        )}
                                                                                                    </Box>
                                                                                                </Box>
                                                                                            }
                                                                                        />
                                                                                    </Grid>

                                                                                    <Grid
                                                                                        item
                                                                                        xs={
                                                                                            6
                                                                                        }
                                                                                    >
                                                                                        <Box>
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
                                                                                        </Box>
                                                                                        <TimeRangeButtonWithContext
                                                                                            hideQuickExtend
                                                                                            btnGroupColor="default"
                                                                                            maxDate={
                                                                                                alertInsight?.datasetEndTime
                                                                                            }
                                                                                            minDate={
                                                                                                alertInsight?.datasetStartTime
                                                                                            }
                                                                                            timezone={
                                                                                                (alert
                                                                                                    .templateProperties
                                                                                                    ?.timezone as string) ||
                                                                                                determineTimezoneFromAlertInEvaluation(
                                                                                                    alertInsight?.templateWithProperties
                                                                                                )
                                                                                            }
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
                                                                                        <AlertJsonEditorModal
                                                                                            isReadOnly
                                                                                            alert={
                                                                                                alert
                                                                                            }
                                                                                            buttonText={t(
                                                                                                "label.view-json"
                                                                                            )}
                                                                                            cancelButtonText={t(
                                                                                                "label.close"
                                                                                            )}
                                                                                            isDisabled={
                                                                                                !algorithmOption
                                                                                            }
                                                                                        />
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
                                                                                            emptyMessage={
                                                                                                !algorithmOption ? (
                                                                                                    <Alert
                                                                                                        severity="info"
                                                                                                        style={{
                                                                                                            marginTop:
                                                                                                                "40px",
                                                                                                        }}
                                                                                                    >
                                                                                                        {t(
                                                                                                            "message.please-select-a-detection-algorithm-first"
                                                                                                        )}
                                                                                                    </Alert>
                                                                                                ) : null
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

                                                                        {algorithmOption ? (
                                                                            <Grid
                                                                                item
                                                                                xs={
                                                                                    12
                                                                                }
                                                                            >
                                                                                <ThresholdSetupV3
                                                                                    alert={
                                                                                        alert
                                                                                    }
                                                                                    alertEvaluation={
                                                                                        evaluation ||
                                                                                        undefined
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
                                                                                    {dimension !==
                                                                                        SelectDimensionsOptions.ENUMERATORS &&
                                                                                        anomalyDetection ===
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
                                                                                </ThresholdSetupV3>
                                                                            </Grid>
                                                                        ) : (
                                                                            <ChartContentV2
                                                                                isSearchEnabled
                                                                                showLoadButton
                                                                                showOnlyActivity
                                                                                alert={
                                                                                    alert
                                                                                }
                                                                                alertEvaluation={
                                                                                    AlertEvaluationStatus ===
                                                                                    ActionStatus.Working
                                                                                        ? null
                                                                                        : evaluation
                                                                                }
                                                                                evaluationTimeRange={{
                                                                                    startTime:
                                                                                        startTime,
                                                                                    endTime:
                                                                                        endTime,
                                                                                }}
                                                                                hideCallToActionPrompt={
                                                                                    false
                                                                                }
                                                                                showDeleteIcon={
                                                                                    false
                                                                                }
                                                                                onAlertPropertyChange={
                                                                                    onAlertPropertyChange
                                                                                }
                                                                                onReloadClick={
                                                                                    handleReloadPreviewClick
                                                                                }
                                                                            />
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
                                                                                alt="Dimension recommender"
                                                                                src={
                                                                                    DimensionImage
                                                                                }
                                                                            />
                                                                        </Grid>
                                                                    </Box>
                                                                )}
                                                            </Grid>
                                                        )}
                                                    {algorithmOption && (
                                                        <Grid container>
                                                            <Box
                                                                marginTop={2}
                                                                width="100%"
                                                            >
                                                                <NotificationView />
                                                            </Box>
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
                            onCancel={() => setOpenCompositeFilterModal(false)}
                            onUpdateCompositeFiltersChange={
                                onUpdateCompositeFiltersChange
                            }
                        />
                    )}
                    {openCreateAlertModal && (
                        <CreateAlertModal
                            defaultCron={alertInsight?.defaultCron}
                            onCancel={() => setOpenCreateAlertModal(false)}
                        />
                    )}
                    {!isNil(selectedTable?.dataset?.id) && (
                        <ColumnsDrawer
                            datasetId={selectedTable?.dataset.id}
                            isOpen={openViewColumnsListDrawer}
                            selectedDataset={selectedTable}
                            onClose={() =>
                                setOpenViewColumnsListDrawer((prev) => !prev)
                            }
                        />
                    )}
                </PageContentsCardV1>
            </Grid>
        </>
    );
};
