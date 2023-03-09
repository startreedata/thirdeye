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
import { DateTime } from "luxon";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    SkeletonV1,
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
} from "../../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import {
    generateDateRangeDaysFromNow,
    getAnomaliesCreatePath,
} from "../../../utils/routes/routes.util";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { WizardBottomBar } from "../../welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { CreateAnomalyPropertiesForm } from "../create-anomaly-properties-form/create-anomaly-properties-form.component";
import { PreviewAnomalyChart } from "../preview-anomaly-chart/preview-anomaly-chart.component";
import {
    CreateAnomalyEditableFormFields,
    CreateAnomalyReadOnlyFormFields,
    CreateAnomalyWizardProps,
    HandleSetFields,
} from "./create-anomaly-wizard.interfaces";
import {
    createEditableAnomaly,
    getAnomaliesAvgValues,
    getEnumerationItemsConfigFromAlert,
    getIsAnomalyValid,
} from "./create-anomaly-wizard.utils";

export const CreateAnomalyWizard: FunctionComponent<CreateAnomalyWizardProps> =
    ({ alerts, submitBtnLabel, cancelBtnLabel, onSubmit, onCancel }) => {
        const { t } = useTranslation();
        const [searchParams, setSearchParams] = useSearchParams();
        const { id: selectedAlertId } = useParams<{ id: string }>();
        const navigate = useNavigate();
        const { notify } = useNotificationProviderV1();

        const selectedAlert = alerts?.find(
            (a) => a.id === Number(selectedAlertId)
        );

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
                // TODO: Implement initialAnomalyData
                alert: selectedAlert || null,
                enumerationItem: null,
                // These dateRange values are meaningless without a selectedAlert, and once
                // the alert insights are fetched, this is set to values from that data
                // anyways, so these are just dummy values to prevent initiation errors
                dateRange: [0, 0],
            });

        const readOnlyFormFields =
            useMemo<CreateAnomalyReadOnlyFormFields>(() => {
                if (!formFields.alert) {
                    return {
                        dataSource: null,
                        dataset: null,
                        metric: null,
                    };
                }
                const {
                    dataSource,
                    dataset,
                    aggregationColumn: metric,
                } = formFields.alert.templateProperties as {
                    dataSource: string;
                    dataset: string;
                    aggregationColumn: string;
                };

                return {
                    dataSource,
                    dataset,
                    metric,
                };
            }, [formFields.alert]);

        const editableAnomaly: EditableAnomaly | null = useMemo(() => {
            if (!(formFields.alert && formFields.dateRange)) {
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
        }, [formFields, readOnlyFormFields, evaluation]);

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

                // Set the date range to the middle of the anomaly chart by default
                handleSetField(
                    "dateRange",
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
                    )
                );
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
        };

        const isAnomalyValid = useMemo<boolean>(
            () => getIsAnomalyValid(editableAnomaly),
            [editableAnomaly]
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
                                        readOnlyFormFields={readOnlyFormFields}
                                        timezone={timezone}
                                    />
                                    <Grid item xs={12}>
                                        <Box pb={3} pt={2}>
                                            <Divider />
                                        </Box>
                                    </Grid>
                                    <Grid item xs={12}>
                                        <EmptyStateSwitch
                                            emptyState={
                                                <Box
                                                    mt={8}
                                                    p={1}
                                                    position="relative"
                                                >
                                                    <SkeletonV1
                                                        animation={false}
                                                        height={400}
                                                        variant="rect"
                                                        width="100%"
                                                    />
                                                    <Box
                                                        alignItems="center"
                                                        display="flex"
                                                        height={400}
                                                        justifyContent="center"
                                                        position="absolute"
                                                        top={0}
                                                        width="100%"
                                                    >
                                                        <Typography
                                                            color="textSecondary"
                                                            variant="body2"
                                                        >
                                                            {t(
                                                                "message.select-an-alert-to-generate-the-anomaly-preview"
                                                            )}
                                                        </Typography>
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
