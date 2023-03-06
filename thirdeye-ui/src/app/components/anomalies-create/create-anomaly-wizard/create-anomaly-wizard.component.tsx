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
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { EditableAnomaly } from "../../../pages/anomalies-create-page/anomalies-create-page.interfaces";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    SkeletonV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAlertInsight } from "../../../rest/alerts/alerts.actions";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { AnomalyResultSource } from "../../../rest/dto/anomaly.interfaces";
import { Metric } from "../../../rest/dto/metric.interfaces";
import { useGetEnumerationItems } from "../../../rest/enumeration-items/enumeration-items.actions";
import { determineTimezoneFromAlertInEvaluation } from "../../../utils/alerts/alerts.util";
import {
    generateDateRangeDaysFromNow,
    getAnomaliesCreatePath,
} from "../../../utils/routes/routes.util";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
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
    getEnumerationItemsConfigFromAlert,
    getIsAnomalyValid,
} from "./create-anomaly-wizard.utils";

export const CreateAnomalyWizard: FunctionComponent<CreateAnomalyWizardProps> =
    ({
        alerts,

        submitBtnLabel,
        cancelBtnLabel,
        onSubmit,
        onCancel,
        initialAnomalyData, // TODO: Implement initialAnomalyData
    }) => {
        const { t } = useTranslation();
        const [searchParams, setSearchParams] = useSearchParams();
        const { id: selectedAlertId } = useParams<{ id: string }>();
        const navigate = useNavigate();

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
            status: alertInsightStatus,
        } = useGetAlertInsight();

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
            if (
                !(
                    formFields.alert &&
                    formFields.dateRange &&
                    // formFields.enumerationItem &&
                    readOnlyFormFields.dataset &&
                    readOnlyFormFields.metric
                )
            ) {
                return null;
            }

            return {
                sourceType: AnomalyResultSource.USER_LABELED_ANOMALY,
                startTime: formFields.dateRange[0],
                endTime: formFields.dateRange[1],
                ...(formFields.enumerationItem && {
                    enumerationItem: formFields.enumerationItem,
                }),
                alert: formFields.alert,
                metadata: {
                    dataset: { name: readOnlyFormFields.dataset },
                    metric: { name: readOnlyFormFields.metric } as Metric,
                },
                metric: { name: readOnlyFormFields.metric } as Metric,

                // TODO: ?Proper values
                // avgBaselineVal: 0,
                // avgCurrentVal: 0,

                // Hardcoded for Anomaly #366, for testing
                // avgBaselineVal: -0.9689632094628097,
                // avgCurrentVal: 3,

                // score: 0.0,
                // weight: 0.0,
                // impactToGlobal: 0.0,
            };
        }, [formFields, readOnlyFormFields]);

        useEffect(() => {
            // If the alert is valid
            if (formFields.alert) {
                const newAlertId = formFields.alert.id;

                // Get the insights to get the start and end time for the alert
                getAlertInsight({ alertId: Number(newAlertId) });

                // Update the alert id URL param
                navigate(getAnomaliesCreatePath(newAlertId), {
                    replace: true,
                });

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

        // TODO: Remove if not needed for error handling
        // useEffect(() => {
        //     if (selectedAlertId) {
        //         // Get the start and end time for the alert
        //         getAlertInsight({ alertId: Number(selectedAlertId) });
        //     }
        // }, [selectedAlertId]);

        useEffect(() => {
            if (alertInsight) {
                searchParams.set(
                    TimeRangeQueryStringKey.START_TIME,
                    `${alertInsight.datasetStartTime}`
                );
                searchParams.set(
                    TimeRangeQueryStringKey.END_TIME,
                    `${alertInsight.datasetEndTime}`
                );
                setSearchParams(searchParams);

                // TODO: Verify if this is how it should be done
                // Set the date range to the middle of the anomaly chart by default
                handleSetField(
                    "dateRange",
                    generateDateRangeDaysFromNow(
                        2,
                        DateTime.fromSeconds(
                            (alertInsight.datasetStartTime +
                                alertInsight.datasetEndTime) /
                                2_000,
                            {
                                zone: determineTimezoneFromAlertInEvaluation(
                                    alertInsight?.templateWithProperties
                                ),
                            }
                        )
                    )
                );
            }
        }, [alertInsight]);

        const handleCancelClick = (): void => {
            onCancel?.();
        };
        const handleSubmitClick = (): void => {
            editableAnomaly && onSubmit?.(editableAnomaly);
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

        const isAnomalyValid = useCallback(
            () => !!editableAnomaly && getIsAnomalyValid(editableAnomaly),
            [editableAnomaly]
        );

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
                                        Configure details for the anomaly
                                        datetime range and the parent alert
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
                                        timezone={determineTimezoneFromAlertInEvaluation(
                                            alertInsight?.templateWithProperties
                                        )}
                                    />
                                    <Grid item xs={12}>
                                        <Box pb={3} pt={2}>
                                            <Divider />
                                        </Box>
                                    </Grid>
                                    <Grid item xs={12}>
                                        <LoadingErrorStateSwitch
                                            isError={
                                                alertInsightStatus ===
                                                ActionStatus.Error
                                            }
                                            isLoading={
                                                !editableAnomaly ||
                                                alertInsightStatus ===
                                                    ActionStatus.Working
                                            }
                                            loadingState={
                                                <Box p={1} position="relative">
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
                                                            Select an alert to
                                                            preview anomaly
                                                        </Typography>
                                                    </Box>
                                                </Box>
                                            }
                                        >
                                            <PreviewAnomalyChart
                                                editableAnomaly={
                                                    editableAnomaly as EditableAnomaly
                                                }
                                            />
                                        </LoadingErrorStateSwitch>
                                    </Grid>
                                    {/* <Divider />
                                    <pre>
                                        {JSON.stringify(
                                            formFields,
                                            undefined,
                                            4
                                        )}
                                    </pre> */}
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
