/*
 * Copyright 2024 StarTree Inc
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
// external
import React, { ReactNode, useEffect, useMemo } from "react";
import { Box, Button, Divider, Grid, Typography } from "@material-ui/core";
import { Edit } from "@material-ui/icons";
import { useTranslation } from "react-i18next";
import { cloneDeep } from "lodash";

// app components
import {
    PageContentsCardV1,
    useNotificationProviderV1,
} from "../../../../../platform/components";
import { PreviewChartMain } from "../../../../../components/preview-chart";
// state
import { EnumerationItem, useCreateAlertStore } from "../../../hooks/state";

// types
import {
    AlertEvaluation,
    EditableAlert,
} from "../../../../../rest/dto/alert.interfaces";

// utils
import { generateInputFieldConfigsForAlertTemplate } from "../../../../../components/alert-wizard-v3/threshold-setup/threshold-setup.utils";
import {
    createAlertEvaluation,
    determineTimezoneFromAlertInEvaluation,
} from "../../../../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../../../../utils/notifications/notifications.util";

// apis
import { useGetEvaluation } from "../../../../../rest/alerts/alerts.actions";
import { RequiredProperties } from "./required-properties";

export const GraphPlot = (): JSX.Element => {
    const { t } = useTranslation();
    const {
        selectedDetectionAlgorithm,
        workingAlert,
        setWorkingAlert,
        workingAlertEvaluation,
        alertTemplates,
        alertInsight,
        selectedTimeRange,
        setWorkingAlertEvaluation,
        setSelectedEnumerationItems,
        apiState,
        setApiState,
        isEvaluationDataStale,
        setIsEvaluationDataStale,
        setShowDimensionRecommendorModal,
    } = useCreateAlertStore();
    const { notify } = useNotificationProviderV1();
    const { evaluation, getEvaluation, status, errorMessages } =
        useGetEvaluation();
    useEffect(() => {
        setApiState({
            ...apiState,
            evaluationState: {
                ...apiState.evaluationState,
                status,
            },
        });
    }, [status]);

    useEffect(() => {
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alert-evaluation"),
            })
        );
    }, [status]);

    const inputFieldConfigs = useMemo(() => {
        const selectedTemplate = alertTemplates?.find(
            (alertTemplateCandidate) => {
                return (
                    alertTemplateCandidate.name ===
                    (workingAlert as EditableAlert).template?.name
                );
            }
        );
        if (selectedTemplate) {
            return generateInputFieldConfigsForAlertTemplate(selectedTemplate);
        }

        return [];
    }, [(workingAlert as EditableAlert).template?.name]);

    const handleReload = (): void => {
        const clonedAlert = cloneDeep(workingAlert) as EditableAlert;
        const hasEnumerationItems =
            !!clonedAlert.templateProperties?.enumeratorQuery ||
            !!clonedAlert.templateProperties?.enumerationItems;
        getEvaluation(
            createAlertEvaluation(
                clonedAlert,
                selectedTimeRange!.startTime!,
                selectedTimeRange!.endTime!,
                {
                    listEnumerationItemsOnly: hasEnumerationItems,
                }
            )
        );
        setIsEvaluationDataStale(false);
    };

    const handleAlertPropertyChange = (alert: Partial<EditableAlert>): void => {
        const tempplateProperties = alert.templateProperties;
        if (tempplateProperties) {
            const clonedWorkingAlert = cloneDeep(workingAlert) as EditableAlert;
            clonedWorkingAlert.templateProperties = tempplateProperties;
            setWorkingAlert(clonedWorkingAlert);
            if (tempplateProperties.enumerationItems) {
                setSelectedEnumerationItems(
                    tempplateProperties.enumerationItems as EnumerationItem[]
                );
            }
        }
    };

    useEffect(() => {
        if (evaluation) {
            setWorkingAlertEvaluation(evaluation);
        }
    }, [evaluation]);

    const editDimesnionsCTA = (): ReactNode => {
        return (
            <Button
                color="primary"
                startIcon={<Edit />}
                variant="outlined"
                onClick={() => {
                    setShowDimensionRecommendorModal(true);
                }}
            >
                {t("label.edit-entity", { entity: t("label.dimensions") })}
            </Button>
        );
    };

    return (
        <PageContentsCardV1>
            {selectedDetectionAlgorithm && (
                <>
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="space-between"
                    >
                        <Grid item>
                            <Typography variant="h5">
                                {selectedDetectionAlgorithm &&
                                    t("label.entity-setup", {
                                        entity: selectedDetectionAlgorithm
                                            .algorithmOption.title,
                                        multidimension:
                                            selectedDetectionAlgorithm
                                                .algorithmOption
                                                .alertTemplateForMultidimension ===
                                            (workingAlert as EditableAlert)
                                                .template?.name
                                                ? `(${t(
                                                      "label.multidimension"
                                                  )})`
                                                : "",
                                    })}
                            </Typography>
                        </Grid>
                    </Grid>

                    {inputFieldConfigs.length > 0 && (
                        <Grid item xs={12}>
                            <Box padding={2} />
                        </Grid>
                    )}

                    <RequiredProperties />

                    <Grid item xs={12}>
                        <Box marginBottom={2} marginTop={2} padding={1}>
                            <Divider />
                        </Box>
                    </Grid>
                </>
            )}
            <PreviewChartMain
                hideCallToActionPrompt
                additionalCTA={editDimesnionsCTA()}
                alert={workingAlert as EditableAlert}
                alertEvaluation={workingAlertEvaluation as AlertEvaluation}
                dateRange={{
                    startTime: alertInsight!.defaultStartTime!,
                    endTime: alertInsight!.defaultEndTime!,
                    timezone: determineTimezoneFromAlertInEvaluation(
                        alertInsight?.templateWithProperties
                    )!,
                }}
                getEvaluationDataStatus={apiState.evaluationState!.status!}
                isEvaluationDataStale={isEvaluationDataStale}
                showOnlyActivity={!selectedDetectionAlgorithm}
                showTimeRange={false}
                onAlertPropertyChange={handleAlertPropertyChange}
                onReloadData={handleReload}
            />
        </PageContentsCardV1>
    );
};
