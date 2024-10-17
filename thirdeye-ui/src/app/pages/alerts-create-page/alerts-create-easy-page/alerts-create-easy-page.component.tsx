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
import { Icon } from "@iconify/react";
import {
    Box,
    Button,
    CircularProgress,
    Divider,
    Grid,
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
import { Autocomplete } from "@material-ui/lab";
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
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { AvailableAlgorithmOption } from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.interfaces";
import {
    generateAvailableAlgorithmOptions,
    generateAvailableAlgorithmOptionsForRecommendations,
} from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.utils";
import { AnomaliesFilterConfiguratorRenderConfigs } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.interfaces";
import { getAvailableFilterOptions } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.utils";
import { ChartContentV2 } from "../../../components/alert-wizard-v3/preview-chart/chart-content-v2/chart-content-v2.component";
import {
    generateTemplateProperties,
    GranularityValue,
} from "../../../components/alert-wizard-v3/select-metric/select-metric.utils";
import { ThresholdSetup } from "../../../components/alert-wizard-v3/threshold-setup/threshold-setup.component";
import { ColumnsDrawer } from "../../../components/columns-drawer/columns-drawer.component";
import { CreateAlertModal } from "../../../components/create-alert-modal/create-alert-modal.component";
import { InputSectionV2 } from "../../../components/form-basics/input-section-v2/input-section-v2.component";
import { RadioSection } from "../../../components/form-basics/radio-section-v2/radio-section.component";
import { RadioSectionOptions } from "../../../components/form-basics/radio-section-v2/radio-section.interfaces";
import { alertsBasicHelpCards } from "../../../components/help-drawer-v1/help-drawer-card-contents.utils";
import { HelpDrawerV1 } from "../../../components/help-drawer-v1/help-drawer-v1.component";
import { TimeRangeButtonWithContext } from "../../../components/time-range/time-range-button-with-context-v2/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { ReactComponent as FilterListRoundedIcon } from "../../../platform/assets/images/filter-icon.svg";
import {
    PageContentsCardV1,
    PageHeaderActionsV1,
} from "../../../platform/components";
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
import { getAlertsAllPath } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../../alerts-create-guided-page/alerts-create-guided-page.interfaces";
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

    const [showSQLWhere, setShowSQLWhere] = useState(false);
    const [enumerations, setEnumerations] = useState(false);
    const [dimension, setDimension] = useState<string | null>(null);

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
                if (alert.templateProperties?.enumeratoryQuery) {
                    setEnumerators(
                        String(alert.templateProperties?.enumeratoryQuery)
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
        item: { label: string; value: GranularityValue }
    ): Promise<void> => {
        const existingGranularity = granularity;
        setGranularity(item.value);
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
                              ...alert.templateProperties,
                              ...generateTemplateProperties(
                                  isCustomMetrics
                                      ? editedDatasourceFieldValue
                                      : (selectedMetric as string),
                                  selectedTable?.dataset,
                                  aggregationFunction || "",
                                  item.value
                              ),
                              queryFilters: queryFilters,
                              enumeratoryQuery:
                                  dimension ===
                                  SelectDimensionsOptions.ENUMERATORS
                                      ? enumerators
                                      : null,
                          },
                      };
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
        setIsMultiDimensionAlert(item === AnomalyDetectionOptions.COMPOSITE);
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
                onClick: () => {
                    setDimension(() => {
                        if (item === SelectDimensionsOptions.ENUMERATORS) {
                            setEnumerators("");
                            setEnumerations(false);
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

    useEffect(() => {
        let isCustomMetrics = false;
        if (selectedMetric === t("label.custom-metric-aggregation")) {
            isCustomMetrics = true;
        }
        if (
            selectedMetric &&
            selectedTable &&
            granularity &&
            (aggregationFunction ||
                (isCustomMetrics && editedDatasourceFieldValue))
        ) {
            const isAnomalyDetectionComposite =
                anomalyDetection === AnomalyDetectionOptions.COMPOSITE;
            const isRecommendation =
                algorithmOption?.recommendationLabel ===
                t("label.recommended-configuration");
            const recommendedTemplate = isRecommendation
                ? alertRecommendations.find(
                      (rec, index) =>
                          `${rec.alert.template?.name}-${index}` ===
                          algorithmOption?.recommendationId
                  )
                : null;
            const workingAlert = {
                template: {
                    name:
                        (isAnomalyDetectionComposite
                            ? algorithmOption?.algorithmOption
                                  .alertTemplateForMultidimension
                            : algorithmOption?.algorithmOption
                                  ?.alertTemplate) ||
                        (!isAnomalyDetectionComposite
                            ? createNewStartingAlert().template?.name
                            : dimension === SelectDimensionsOptions.ENUMERATORS
                            ? ALERT_TEMPLATE_FOR_EVALUATE_QUERY_DX
                            : ALERT_TEMPLATE_FOR_EVALUATE_DX),
                },
                templateProperties: {
                    ...alert.templateProperties,
                    ...(recommendedTemplate?.alert.templateProperties ?? {}),
                    ...generateTemplateProperties(
                        isCustomMetrics
                            ? editedDatasourceFieldValue
                            : (selectedMetric as string),
                        selectedTable?.dataset,
                        aggregationFunction || "",
                        granularity
                    ),
                    queryFilters: queryFilters,
                    enumeratoryQuery:
                        dimension === SelectDimensionsOptions.ENUMERATORS
                            ? enumerators
                            : null,
                },
            };
            onAlertPropertyChange(workingAlert);
            if (
                !algorithmOption &&
                (!isAnomalyDetectionComposite || !!compositeFilters)
            ) {
                getAlertRecommendation({ ...alert, ...workingAlert });
            }
            handleReloadPreviewClick(workingAlert);
        }
    }, [
        selectedMetric,
        selectedTable,
        granularity,
        aggregationFunction,
        algorithmOption,
        compositeFilters,
        queryFilters,
        anomalyDetection,
        enumerations,
    ]);

    const { getEvaluation, evaluation } = useGetEvaluation();

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
                enumeratoryQuery:
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
        setCompositeFilters(template);
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
                                            <PageHeaderActionsV1>
                                                <HelpDrawerV1
                                                    cards={alertsBasicHelpCards}
                                                    title={`${t(
                                                        "label.need-help"
                                                    )}?`}
                                                    trigger={(handleOpen) => (
                                                        <Button
                                                            className={
                                                                classes.infoButton
                                                            }
                                                            color="primary"
                                                            size="small"
                                                            variant="outlined"
                                                            onClick={handleOpen}
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
                                                    label={t("label.dataset")}
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
                                                            "label.detection-type"
                                                        )}
                                                        options={getAnomalyDetectionOptions(
                                                            [
                                                                AnomalyDetectionOptions.SINGLE,
                                                                AnomalyDetectionOptions.COMPOSITE,
                                                            ]
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
                                                                                setEnumerations(
                                                                                    true
                                                                                )
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
                                                                                                        classes.animatedBorder
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
                                                                                                        setAlgorithmOption(
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
                                                                                                            "label.recommended-algorithm"
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

                                                                        {algorithmOption ? (
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
                                                                                </ThresholdSetup>
                                                                            </Grid>
                                                                        ) : (
                                                                            <ChartContentV2
                                                                                showLoadButton
                                                                                showOnlyActivity
                                                                                alert={
                                                                                    alert
                                                                                }
                                                                                alertEvaluation={
                                                                                    evaluation
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
