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

import { Box, Divider, Grid, Typography } from "@material-ui/core";
import { Alert as MuiAlert } from "@material-ui/lab";
import { DateTime } from "luxon";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { ReactComponent as ChartSkeleton } from "../../../../assets/images/chart-skeleton.svg";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    useGetAlertInsight,
    useGetEvaluation,
} from "../../../rest/alerts/alerts.actions";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { EditableAnomaly } from "../../../rest/dto/anomaly.interfaces";
import { useGetEnumerationItems } from "../../../rest/enumeration-items/enumeration-items.actions";
import {
    createAlertEvaluation,
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
} from "../../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import {
    generateDateRangeDaysFromNow,
    getAnomaliesCreatePath,
} from "../../../utils/routes/routes.util";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { ZoomDomain } from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { WizardBottomBar } from "../../welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { CreateAnomaliesDateRangePicker } from "../create-anomalies-date-range-picker/create-anomalies-date-range-picker.componnent";
import { CreateAnomalyPropertiesForm } from "../create-anomaly-properties-form/create-anomaly-properties-form.component";
import { PreviewAnomalyChart } from "../preview-anomaly-chart/preview-anomaly-chart.component";
import {
    CreateAnomalyEditableFormFields,
    CreateAnomalyWizardProps,
    HandleSetFields,
    SelectedAlertDetails,
} from "./create-anomaly-wizard.interfaces";
import { useCreateAnomalyWizardStyles } from "./create-anomaly-wizard.styles";
import {
    AnomalyWizardQueryParams,
    createEditableAnomaly,
    getAnomaliesAvgValues,
    getEnumerationItemsConfigFromAlert,
    getIsAnomalyValid,
} from "./create-anomaly-wizard.utils";

export const CreateAnomalyWizard: FunctionComponent<CreateAnomalyWizardProps> =
    ({ alerts, submitBtnLabel, cancelBtnLabel, onSubmit, onCancel }) => {
        const classes = useCreateAnomalyWizardStyles();
        const { t } = useTranslation();
        const [searchParams, setSearchParams] = useSearchParams();
        const { id: selectedAlertId } = useParams<{ id: string }>();
        const navigate = useNavigate();
        const { notify } = useNotificationProviderV1();

        const selectedAlert =
            (selectedAlertId &&
                alerts?.find((a) => a.id === Number(selectedAlertId))) ||
            null;

        const {
            enumerationItems,
            getEnumerationItems,
            status: enumerationItemsStatus,
        } = useGetEnumerationItems();

        const {
            alertInsight,
            getAlertInsight,
            status: alertInsightRequestStatus,
            errorMessages: alertInsightErrorMessage,
        } = useGetAlertInsight();

        const {
            evaluation,
            getEvaluation,
            errorMessages: evaluationErrorMessages,
            status: getEvaluationRequestStatus,
        } = useGetEvaluation();

        const [formFields, setFormFields] =
            useState<CreateAnomalyEditableFormFields>({
                alert: selectedAlert,
                enumerationItem: null,
                // These dateRange values are meaningless without a selectedAlert, and once
                // the alert insights are fetched, this is set to values from that data
                // anyways, so these are just dummy values to prevent initiation errors
                dateRange: [0, 0],
            });
        const [captureDateRangeFromChart, setCaptureDateRangeFromChart] =
            useState(false);

        const selectedAlertDetails =
            useMemo<SelectedAlertDetails | null>(() => {
                if (!formFields.alert) {
                    return null;
                }
                const {
                    dataSource,
                    dataset,
                    aggregationColumn,
                    aggregationFunction,
                } = formFields.alert.templateProperties as {
                    dataSource: string;
                    dataset: string;
                    aggregationColumn: string;
                    aggregationFunction: string;
                };

                const metric = `${aggregationFunction}(${aggregationColumn})`;

                return {
                    dataSource,
                    dataset,
                    metric,
                };
            }, [formFields.alert]);

        const alertHasEnumerationItems = !!(
            formFields.alert &&
            getEnumerationItemsConfigFromAlert(formFields.alert)
        );

        const editableAnomaly: EditableAnomaly | null = useMemo(() => {
            if (!(formFields.alert && formFields.dateRange)) {
                return null;
            }

            // If the alert has enumeration items, the editableAnomaly cannot
            // be created without having a dimension selected first
            if (alertHasEnumerationItems && !formFields.enumerationItem) {
                return null;
            }

            return createEditableAnomaly({
                alert: formFields.alert,
                enumerationItemId: formFields.enumerationItem?.id,
                startTime: formFields.dateRange[0],
                endTime: formFields.dateRange[1],
                ...(evaluation &&
                    getAnomaliesAvgValues({
                        evaluation,
                        startTime: formFields.dateRange[0],
                    })),
            });
        }, [formFields, evaluation]);

        const fetchAlertEvaluation = (): void => {
            const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
            const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);

            if (!formFields.alert || !start || !end) {
                return;
            }
            getEvaluation(
                createAlertEvaluation(
                    formFields.alert,
                    Number(start),
                    Number(end)
                ),
                undefined,
                formFields.enumerationItem || undefined
            );
        };

        useEffect(() => {
            // If the alert is valid
            if (formFields.alert) {
                const newAlertId = formFields.alert.id;

                // Update the alert id URL param IF it is different from the new selected alert ID
                if (Number(selectedAlertId) !== newAlertId) {
                    navigate(getAnomaliesCreatePath(newAlertId), {
                        replace: true,
                    });
                }
                // Get the insights to get the start and end time for the alert
                getAlertInsight({ alertId: Number(newAlertId) });

                // Fetch the enumeration items for this alert
                if (
                    getEnumerationItemsConfigFromAlert(formFields.alert)?.length
                ) {
                    getEnumerationItems({
                        alertId: formFields.alert.id,
                    }).then((enumerationItemsProp) => {
                        if (!enumerationItemsProp) {
                            return;
                        }

                        // From query params
                        const selectedEnumerationItemId =
                            Number(
                                searchParams.get(
                                    AnomalyWizardQueryParams.EnumerationItemId
                                )
                            ) || null;

                        // If the enumeration item query param is defined AND the form field
                        // enumeration item is empty, use the query param ID. The query
                        // param should always be empty if the user is manually selecting a
                        // new alert, but if redirecting from another page, the query param
                        // may be present, so should be automatically loaded.

                        if (
                            selectedEnumerationItemId &&
                            !formFields.enumerationItem
                        ) {
                            const selectedEnumerationItem =
                                enumerationItemsProp?.find(
                                    (e) => e.id === selectedEnumerationItemId
                                ) || null;

                            // Will take care of updating the enumeration item and
                            // corresponding search param for both "defined" and "not defined" cases
                            handleSetField(
                                "enumerationItem",
                                selectedEnumerationItem
                            );
                        }
                    });
                }
            }
        }, [formFields.alert]);

        useEffect(() => {
            // Fetch alert evaluation when an enumeration item is changed
            fetchAlertEvaluation();
        }, [formFields.enumerationItem]);

        useEffect(() => {
            if (alertInsight) {
                let start = Number(
                    searchParams.get(TimeRangeQueryStringKey.START_TIME)
                );
                let end = Number(
                    searchParams.get(TimeRangeQueryStringKey.END_TIME)
                );

                // If the start AND end params do not exist, set them from the alert insights
                if (!(start && end)) {
                    start = alertInsight.datasetStartTime;
                    end = alertInsight.datasetEndTime;

                    searchParams.set(
                        TimeRangeQueryStringKey.START_TIME,
                        `${start}`
                    );
                    searchParams.set(
                        TimeRangeQueryStringKey.END_TIME,
                        `${end}`
                    );
                    setSearchParams(searchParams);
                }

                // Fetch the alert evaluation AFTER the datetime query params are extracted from
                // `alertInsight` and set to avoid duplicate API calls with outdated datetime params
                fetchAlertEvaluation();

                let dateRangeValues: CreateAnomalyEditableFormFields["dateRange"] =
                    generateDateRangeDaysFromNow(
                        1,
                        DateTime.fromMillis((start + end) / 2, {
                            zone: determineTimezoneFromAlertInEvaluation(
                                alertInsight?.templateWithProperties
                            ),
                        }).set({
                            hour: 0,
                            minute: 0,
                            second: 0,
                            millisecond: 0,
                        }) // Remove any offsets in the middle value
                    );

                // Load params from query params if present
                if (
                    searchParams.has(
                        AnomalyWizardQueryParams.AnomalyStartTime
                    ) &&
                    searchParams.has(AnomalyWizardQueryParams.AnomalyEndTime)
                ) {
                    const anomalyStartTimeQueryParam = Number(
                        searchParams.get(
                            AnomalyWizardQueryParams.AnomalyStartTime
                        )
                    );
                    const anomalyEndTimeQueryParam = Number(
                        searchParams.get(
                            AnomalyWizardQueryParams.AnomalyEndTime
                        )
                    );

                    // Pick from query params if present
                    if (
                        anomalyStartTimeQueryParam &&
                        anomalyEndTimeQueryParam
                    ) {
                        dateRangeValues = [
                            anomalyStartTimeQueryParam,
                            anomalyEndTimeQueryParam,
                        ];
                    }
                }

                // Set the date range to the middle of the anomaly chart by default
                handleSetField("dateRange", dateRangeValues);
            }
        }, [alertInsight]);

        const handleCancelClick = (): void => {
            onCancel?.();
        };
        const handleSubmitClick = (): void => {
            if (editableAnomaly) {
                onSubmit?.(editableAnomaly);
            } else {
                notify(
                    NotificationTypeV1.Error,
                    t("message.invalid-entity-data", {
                        entity: t("label.anomaly"),
                    })
                );
            }
        };

        const findClosestAppropriateTimestamp = (
            thresholdValue: number
        ): number | null => {
            // This function is only useful if evaluation is defined
            if (!evaluation) {
                return null;
            }

            const detectionEvaluation =
                extractDetectionEvaluation(evaluation)[0];
            const { timestamp } = detectionEvaluation.data;

            const nextClosestTimestamp = timestamp.find(
                (v) => v >= thresholdValue
            );

            // If the list does not have any value greater than thresholdValue, return null
            if (!nextClosestTimestamp) {
                return null;
            }

            return nextClosestTimestamp;
        };

        const handleRangeSelection = (
            zoomDomain: ZoomDomain | null
        ): boolean => {
            if (!captureDateRangeFromChart) {
                // Proceed with the default zoom action
                return true;
            }

            // Disable the drag-select
            setCaptureDateRangeFromChart(false);
            if (zoomDomain?.x0 && zoomDomain?.x1 && evaluation) {
                const detectionEvaluation =
                    extractDetectionEvaluation(evaluation)[0];
                const { timestamp } = detectionEvaluation.data;

                const anomalyStartTimestamp = timestamp.find(
                    (t) => t >= zoomDomain.x0
                );
                const anomalyEndTimestamp = timestamp.find(
                    (t) => t >= zoomDomain.x1
                );

                if (anomalyStartTimestamp && anomalyEndTimestamp) {
                    handleSetField("dateRange", [
                        anomalyStartTimestamp,
                        anomalyEndTimestamp,
                    ]);

                    // Cancel the zoom
                    return false;
                }
            }

            notify(
                NotificationTypeV1.Error,
                "Unable to parse date range from the chart. Please try again."
            );

            // Cancel the zoom
            return false;
        };

        const handleUpdateEnumerationItemQueryParam = (
            enumerationItemProp: CreateAnomalyEditableFormFields["enumerationItem"]
        ): void => {
            // If the param passed is truthy
            if (enumerationItemProp) {
                searchParams.set(
                    AnomalyWizardQueryParams.EnumerationItemId,
                    `${enumerationItemProp.id}`
                );
            } else {
                searchParams.delete(AnomalyWizardQueryParams.EnumerationItemId);
            }
            setSearchParams(searchParams);
        };

        const handleUpdateAnomalyDateRangeQueryParam = (
            anomalyDateRangeProp: CreateAnomalyEditableFormFields["dateRange"]
        ): void => {
            searchParams.set(
                AnomalyWizardQueryParams.AnomalyStartTime,
                `${anomalyDateRangeProp[0]}`
            );
            searchParams.set(
                AnomalyWizardQueryParams.AnomalyEndTime,
                `${anomalyDateRangeProp[1]}`
            );
            setSearchParams(searchParams);
        };

        /* Common updater function to update the state and handle the side effects */
        const handleSetField: HandleSetFields = (fieldName, fieldValue) => {
            setFormFields((stateProp) => ({
                ...stateProp,
                [fieldName]: fieldValue,

                // Clear out enumeration items if alert is set
                ...(fieldName === "alert" &&
                    !!((fieldValue as Alert).id !== stateProp.alert?.id) && {
                        enumerationItem: null,
                    }),
            }));

            // Handle the side effects
            if (fieldName === "alert") {
                handleUpdateEnumerationItemQueryParam(null);
            } else if (fieldName === "enumerationItem") {
                handleUpdateEnumerationItemQueryParam(
                    fieldValue as CreateAnomalyEditableFormFields["enumerationItem"]
                );
            } else if (fieldName === "dateRange") {
                handleUpdateAnomalyDateRangeQueryParam(
                    fieldValue as CreateAnomalyEditableFormFields["dateRange"]
                );
            }
        };

        const isAnomalyValid = useMemo<boolean>(
            () => getIsAnomalyValid(editableAnomaly, alertHasEnumerationItems),
            [editableAnomaly, alertHasEnumerationItems]
        );

        const timezone = useMemo(
            () =>
                determineTimezoneFromAlertInEvaluation(
                    alertInsight?.templateWithProperties
                ),
            [alertInsight]
        );

        useEffect(() => {
            notifyIfErrors(
                alertInsightRequestStatus,
                alertInsightErrorMessage,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.chart-data"),
                })
            );
        }, [alertInsightErrorMessage, alertInsightRequestStatus]);

        useEffect(() => {
            notifyIfErrors(
                getEvaluationRequestStatus,
                evaluationErrorMessages,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.chart-data"),
                })
            );
        }, [evaluationErrorMessages, getEvaluationRequestStatus]);

        return (
            <>
                <PageContentsGridV1 fullHeight>
                    <Grid item xs={12}>
                        <PageContentsCardV1 fullHeight>
                            <Grid container alignItems="stretch">
                                <Grid item xs={12}>
                                    <Typography variant="h5">
                                        {t("label.setup-entity", {
                                            entity: t("label.anomaly"),
                                        })}
                                    </Typography>
                                    <Typography
                                        color="secondary"
                                        variant="subtitle1"
                                    >
                                        {t(
                                            "message.configure-the-parent-alert-and-the-occurrence-date-time-for-the-anomalous-behavior"
                                        )}
                                    </Typography>
                                </Grid>

                                <Grid item xs={12}>
                                    <CreateAnomalyPropertiesForm
                                        alerts={alerts}
                                        enumerationItemsForAlert={
                                            enumerationItems || []
                                        }
                                        enumerationItemsStatus={
                                            enumerationItemsStatus
                                        }
                                        formFields={formFields}
                                        handleSetField={handleSetField}
                                        selectedAlertDetails={
                                            selectedAlertDetails
                                        }
                                    />
                                    <Grid item xs={12}>
                                        <Box pb={3} pt={2}>
                                            <Divider />
                                        </Box>
                                    </Grid>
                                    <Grid item xs={12}>
                                        <EmptyStateSwitch
                                            emptyState={null}
                                            isEmpty={!timezone}
                                        >
                                            <CreateAnomaliesDateRangePicker
                                                captureDateRangeFromChart={
                                                    captureDateRangeFromChart
                                                }
                                                findClosestAppropriateTimestamp={
                                                    findClosestAppropriateTimestamp
                                                }
                                                formFields={formFields}
                                                handleSetField={handleSetField}
                                                setCaptureDateRangeFromChart={
                                                    setCaptureDateRangeFromChart
                                                }
                                                timezone={timezone as string}
                                            />
                                        </EmptyStateSwitch>
                                    </Grid>
                                    <Grid item xs={12}>
                                        <EmptyStateSwitch
                                            emptyState={
                                                <Box
                                                    mt={2}
                                                    p={1}
                                                    position="relative"
                                                >
                                                    <ChartSkeleton />
                                                    <Box
                                                        alignItems="center"
                                                        display="flex"
                                                        height={200}
                                                        justifyContent="center"
                                                        position="absolute"
                                                        top={0}
                                                        width="100%"
                                                    >
                                                        <MuiAlert
                                                            className={
                                                                classes.infoAlert
                                                            }
                                                            severity="info"
                                                        >
                                                            {formFields.alert &&
                                                            alertHasEnumerationItems &&
                                                            !formFields.enumerationItem
                                                                ? t(
                                                                      "message.select-a-dimension-to-generate-the-anomaly-preview"
                                                                  )
                                                                : t(
                                                                      "message.select-an-alert-to-generate-the-anomaly-preview"
                                                                  )}
                                                        </MuiAlert>
                                                    </Box>
                                                </Box>
                                            }
                                            isEmpty={!editableAnomaly}
                                        >
                                            <PreviewAnomalyChart
                                                editableAnomaly={
                                                    editableAnomaly as EditableAnomaly
                                                }
                                                evaluation={evaluation}
                                                fetchAlertEvaluation={
                                                    fetchAlertEvaluation
                                                }
                                                isLoading={[
                                                    alertInsightRequestStatus,
                                                    getEvaluationRequestStatus,
                                                    enumerationItemsStatus,
                                                ].some(
                                                    (s) =>
                                                        s ===
                                                        ActionStatus.Working
                                                )}
                                                timezone={timezone}
                                                onRangeSelection={
                                                    handleRangeSelection
                                                }
                                            />
                                        </EmptyStateSwitch>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </PageContentsCardV1>
                    </Grid>
                </PageContentsGridV1>
                <WizardBottomBar
                    backButtonLabel={cancelBtnLabel}
                    handleBackClick={handleCancelClick}
                    handleNextClick={handleSubmitClick}
                    nextButtonIsDisabled={!isAnomalyValid}
                    nextButtonLabel={submitBtnLabel}
                />
            </>
        );
    };
