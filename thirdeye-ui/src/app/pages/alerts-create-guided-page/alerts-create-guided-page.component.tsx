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
import { Grid, Typography } from "@material-ui/core";
import { default as React, FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useLocation } from "react-router-dom";
import { generateAvailableAlgorithmOptions } from "../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.utils";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    StepperV1,
} from "../../platform/components";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { generateGenericNameForAlert } from "../../utils/alerts/alerts.util";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { AlertsCreateGuidedPageProps } from "./alerts-create-guided-page.interfaces";

const STEPS = [
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE,
        translationLabel: "select-alert-type",
    },
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING,
        translationLabel: "setup-alert-monitoring",
    },
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS,
        translationLabel: "setup-alert-details",
    },
];

const MULTI_DIMENSION_SELECT_STEP = {
    subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION,
    translationLabel: "multidimension-setup",
};

export const CreateAlertGuidedPage: FunctionComponent<AlertsCreateGuidedPageProps> =
    ({
        alert,
        alertTemplates,
        onAlertPropertyChange,
        onSubmit,
        isCreatingAlert,
        emails,
        setEmails,
        getAlertTemplates,
    }) => {
        const { t } = useTranslation();
        const { pathname } = useLocation();

        // Ensure to filter for what is available on the server
        const [simpleOptions, advancedOptions] = useMemo(() => {
            if (!alertTemplates) {
                return [[], []];
            }
            const availableTemplateNames = alertTemplates.map(
                (alertTemplate) => alertTemplate.name
            );

            return generateAvailableAlgorithmOptions(t, availableTemplateNames);
        }, [alertTemplates]);

        const stepsToDisplay = useMemo(() => {
            const matchingDimensionExploration = [
                ...simpleOptions,
                ...advancedOptions,
            ].find(
                (c) =>
                    c.algorithmOption.alertTemplateForMultidimension ===
                    alert.template?.name
            );

            if (matchingDimensionExploration) {
                return [
                    STEPS[0],
                    MULTI_DIMENSION_SELECT_STEP,
                    ...STEPS.slice(1),
                ];
            }

            return [...STEPS];
        }, [alert]);

        const activeStep = useMemo(() => {
            const activeStepDefinition = stepsToDisplay.find((candidate) =>
                pathname.includes(candidate.subPath)
            );

            if (!activeStepDefinition) {
                return "";
            }

            return activeStepDefinition.subPath;
        }, [pathname, stepsToDisplay]);

        const selectedAlgorithmOption = useMemo(() => {
            return [...simpleOptions, ...advancedOptions].find(
                (c) =>
                    c.algorithmOption.alertTemplate === alert.template?.name ||
                    c.algorithmOption.alertTemplateForPercentile ===
                        alert.template?.name ||
                    c.algorithmOption.alertTemplateForMultidimension ===
                        alert.template?.name
            );
        }, [alert, simpleOptions, advancedOptions]);

        const handleCreateAlertClick = (
            alertFromChild: EditableAlert
        ): void => {
            onSubmit &&
                onSubmit(
                    alertFromChild,
                    generateGenericNameForAlert(
                        alert.templateProperties.aggregationColumn as string,
                        alert.templateProperties.aggregationFunction as string,
                        selectedAlgorithmOption?.algorithmOption
                            .title as string,
                        selectedAlgorithmOption?.algorithmOption
                            .alertTemplateForMultidimension ===
                            alert?.template?.name
                    )
                );
        };

        return (
            <>
                <PageContentsGridV1>
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            <Typography variant="h6">
                                {t("message.complete-the-following-steps")}
                            </Typography>
                            <StepperV1
                                activeStep={activeStep}
                                stepLabelFn={(step: string): string => {
                                    const stepDefinition = stepsToDisplay.find(
                                        (candidate) =>
                                            candidate.subPath === step
                                    );

                                    return t(
                                        `message.${stepDefinition?.translationLabel}`
                                    );
                                }}
                                steps={stepsToDisplay.map(
                                    (item) => item.subPath
                                )}
                            />
                        </PageContentsCardV1>
                    </Grid>
                </PageContentsGridV1>

                <Outlet
                    context={{
                        alert,
                        onAlertPropertyChange,
                        simpleOptions,
                        advancedOptions,
                        selectedAlgorithmOption,
                        emails: emails ?? [],
                        setEmails: setEmails ?? (() => null),
                        handleCreateAlertClick,
                        isCreatingAlert,
                        getAlertTemplates,
                        alertTemplates,
                    }}
                />
            </>
        );
    };
