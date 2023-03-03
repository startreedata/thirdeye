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
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { EditedAnomaly } from "../../../pages/anomalies-create-page/anomalies-create-page.interfaces";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    SkeletonV1,
} from "../../../platform/components";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { AnomalyResultSource } from "../../../rest/dto/anomaly.interfaces";
import { Metric } from "../../../rest/dto/metric.interfaces";
import { useGetEnumerationItems } from "../../../rest/enumeration-items/enumeration-items.actions";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WizardBottomBar } from "../../welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { CreateAnomalyPropertiesForm } from "../create-anomaly-properties-form/create-anomaly-properties-form.component";
import { PreviewAnomalyChart } from "../preview-anomaly-chart/preview-anomaly-chart.component";
import {
    CreateAnomalyEditableFormFields,
    CreateAnomalyReadOnlyFormFields,
    CreateAnomalyWizardProps,
    HandleSetFields,
} from "./create-anomaly-wizard.interfaces";
import { getEnumerationItemsConfigFromAlert } from "./create-anomaly-wizard.utils";

export const CreateAnomalyWizard: FunctionComponent<CreateAnomalyWizardProps> =
    ({
        alerts,

        submitBtnLabel,
        cancelBtnLabel,
        onSubmit,
        onCancel,
        initialAnomalyData,
    }) => {
        const { t } = useTranslation();

        const {
            enumerationItems,
            getEnumerationItems,
            status: enumerationItemsStatus,
        } = useGetEnumerationItems();

        const [formFields, setFormFields] =
            useState<CreateAnomalyEditableFormFields>({
                // TODO: Implement initialAnomalyData
                alert: null,
                enumerationItem: null,
                // Hardcoded for Anomaly #366
                // TODO: Proper values
                dateRange: [1639958400000, 1640044800000],
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

        const editedAnomaly: EditedAnomaly | null = useMemo(() => {
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
                avgBaselineVal: 0,
                avgCurrentVal: 0,

                // Hardcoded for Anomaly #366, for testing
                // avgBaselineVal: -0.9689632094628097,
                // avgCurrentVal: 3,

                score: 0.0,
                weight: 0.0,
                impactToGlobal: 0.0,
            };
        }, [formFields, readOnlyFormFields]);

        useEffect(() => {
            if (
                formFields.alert &&
                getEnumerationItemsConfigFromAlert(formFields.alert)?.length
            ) {
                getEnumerationItems({
                    alertId: formFields.alert.id,
                });
            }
        }, [formFields.alert]);

        const handleCancelClick = (): void => {
            onCancel?.();
        };
        const handleSubmitClick = (): void => {
            editedAnomaly && onSubmit?.(editedAnomaly);
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

        const isAnomalyValid = false;

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
                                    />
                                    <Grid item xs={12}>
                                        <Box pb={3} pt={2}>
                                            <Divider />
                                        </Box>
                                    </Grid>
                                    <Grid item xs={12}>
                                        <LoadingErrorStateSwitch
                                            isError={false}
                                            isLoading={!editedAnomaly}
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
                                                editedAnomaly={
                                                    editedAnomaly as EditedAnomaly
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
