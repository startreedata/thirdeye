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
import { Box, Grid, Typography } from "@material-ui/core";
import { useMutation } from "@tanstack/react-query";
import { default as React, FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useLocation, useOutletContext } from "react-router-dom";
import { generateAvailableAlgorithmOptions } from "../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.utils";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    StepperV1,
} from "../../platform/components";
import { getAlertRecommendation } from "../../rest/alerts/alerts.rest";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { generateGenericNameForAlert } from "../../utils/alerts/alerts.util";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { AlertsSimpleAdvancedJsonContainerPageOutletContextProps } from "../alerts-edit-create-common/alerts-edit-create-common-page.interfaces";
import { useAlertsCreateGuidedPage } from "./alerts-create-guided-page.styles";

const STEPS = [
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_METRIC,
        translationLabel: "select-metric",
    },
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE,
        translationLabel: "select-alert-type",
    },
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_TUNE_ALERT,
        translationLabel: "tune-alert",
    },
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_ANOMALIES_FILTER,
        translationLabel: "setup-anomaly-filters",
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

export const CreateAlertGuidedPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();
    const classes = useAlertsCreateGuidedPage();

    const {
        isLoading,
        data,
        mutate: getRecommendation,
    } = useMutation(getAlertRecommendation);

    const {
        alert,
        handleAlertPropertyChange,
        alertTemplateOptions,
        handleSubmitAlertClick,
        refreshAlertTemplates,
        isEditRequestInFlight,

        selectedSubscriptionGroups,
        handleSubscriptionGroupChange,

        newSubscriptionGroup,
        onNewSubscriptionGroupChange,

        alertInsight,
        getAlertInsight,
        getAlertInsightStatus,
    } =
        useOutletContext<AlertsSimpleAdvancedJsonContainerPageOutletContextProps>();

    const [shouldShowStepper, setShouldShowStepper] = useState(true);

    const [isMultiDimensionAlert, setIsMultiDimensionAlert] = useState(() => {
        return alert?.templateProperties.enumerationItems !== undefined;
    });

    // Ensure to filter for what is available on the server
    const alertTypeOptions = useMemo(() => {
        if (!alertTemplateOptions) {
            return [];
        }
        const availableTemplateNames = alertTemplateOptions.map(
            (alertTemplate) => alertTemplate.name
        );

        return generateAvailableAlgorithmOptions(availableTemplateNames);
    }, [alertTemplateOptions]);

    const stepsToDisplay = useMemo(() => {
        if (isMultiDimensionAlert) {
            return [MULTI_DIMENSION_SELECT_STEP, ...STEPS];
        }

        return [...STEPS];
    }, [isMultiDimensionAlert]);

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
        return alertTypeOptions.find(
            (c) =>
                c.algorithmOption.alertTemplate === alert.template?.name ||
                c.algorithmOption.alertTemplateForPercentile ===
                    alert.template?.name ||
                c.algorithmOption.alertTemplateForMultidimension ===
                    alert.template?.name
        );
    }, [alert, alertTypeOptions]);

    const handleCreateAlertClick = (alertFromChild: EditableAlert): void => {
        handleSubmitAlertClick(
            alertFromChild,
            generateGenericNameForAlert(
                alert.templateProperties.aggregationColumn as string,
                alert.templateProperties.aggregationFunction as string,
                selectedAlgorithmOption?.algorithmOption.title as string,
                selectedAlgorithmOption?.algorithmOption
                    .alertTemplateForMultidimension === alert?.template?.name
            )
        );
    };

    return (
        <>
            <Box display="flex">
                <Box flex="1 2 auto">
                    {shouldShowStepper && (
                        <PageContentsGridV1>
                            <Grid item xs={12}>
                                <PageContentsCardV1>
                                    <Typography variant="h6">
                                        {t(
                                            "message.complete-the-following-steps"
                                        )}
                                    </Typography>
                                    <StepperV1
                                        activeStep={activeStep}
                                        stepLabelFn={(step: string): string => {
                                            const stepDefinition =
                                                stepsToDisplay.find(
                                                    (candidate) =>
                                                        candidate.subPath ===
                                                        step
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
                    )}

                    <Outlet
                        context={{
                            alert,
                            onAlertPropertyChange: handleAlertPropertyChange,
                            alertTypeOptions,
                            selectedAlgorithmOption,
                            handleCreateAlertClick,
                            isCreatingAlert: isEditRequestInFlight,
                            getAlertTemplates: refreshAlertTemplates,
                            alertTemplates: alertTemplateOptions,

                            /**
                             * New Subscription group is used for the case when user
                             * wants to create one single new subscription group
                             * while creating an alert
                             */
                            newSubscriptionGroup,
                            onNewSubscriptionGroupChange,

                            selectedSubscriptionGroups:
                                selectedSubscriptionGroups ?? [],
                            handleSubscriptionGroupChange:
                                handleSubscriptionGroupChange ?? (() => null),

                            alertInsight,
                            getAlertInsight,
                            getAlertInsightStatus,

                            setShouldShowStepper,

                            isMultiDimensionAlert,
                            setIsMultiDimensionAlert,

                            getAlertRecommendation: getRecommendation,
                            getAlertRecommendationIsLoading: isLoading,
                            alertRecommendations: data,
                        }}
                    />
                </Box>
                <Box
                    className={classes.guidedUserFlowPortalContainer}
                    flex="0 0 400px"
                    id="guided-user-flow-portal"
                />
            </Box>
        </>
    );
};
