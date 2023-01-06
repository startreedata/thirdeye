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
import { Box, Button, Grid, Typography } from "@material-ui/core";
import { AxiosError } from "axios";
import { default as React, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AlgorithmSelection } from "../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.component";
import { AvailableAlgorithmOption } from "../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.interfaces";
import { filterOptionWithTemplateNames } from "../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.utils";
import { SampleAlertSelection } from "../../../components/alert-wizard-v3/sample-alert-selection/sample-alert-selection.component";
import { SampleAlertOption } from "../../../components/alert-wizard-v3/sample-alert-selection/sample-alert-selection.interfaces";
import { useAppBarConfigProvider } from "../../../components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { NoDataIndicator } from "../../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { createDefaultAlertTemplates } from "../../../rest/alert-templates/alert-templates.rest";
import { createAlert } from "../../../rest/alerts/alerts.rest";
import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { QUERY_PARAM_KEYS } from "../../../utils/constants/constants.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../../utils/rest/rest.util";
import {
    AppRouteRelative,
    getAlertsAlertViewPath,
    getHomePath,
} from "../../../utils/routes/routes.util";
import { SelectTypePageProps } from "./select-type-page.interface";

export const SelectTypePage: FunctionComponent<SelectTypePageProps> = ({
    sampleAlertsBottom,
    hideSampleAlerts,
    navigateToAlertDetailAfterCreate,
    hideCurrentlySelected,
}) => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { setShowAppNavBar } = useAppBarConfigProvider();
    const { notify } = useNotificationProviderV1();

    const {
        onAlertPropertyChange,
        simpleOptions,
        advancedOptions,
        getAlertTemplates,
        alertTemplates,
        selectedAlgorithmOption,
    } = useOutletContext<{
        alert: EditableAlert;
        onAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
        simpleOptions: AvailableAlgorithmOption[];
        advancedOptions: AvailableAlgorithmOption[];
        getAlertTemplates: () => void;
        alertTemplates: AlertTemplate[];
        selectedAlgorithmOption: AvailableAlgorithmOption;
    }>();

    const handleAlgorithmSelection = (
        isDimensionExploration: boolean
    ): void => {
        if (isDimensionExploration) {
            navigate(
                `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION}`
            );

            return;
        }

        navigate(
            `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`
        );
    };

    const handleCreateDefaultAlertTemplates = (): void => {
        createDefaultAlertTemplates().then(() => {
            getAlertTemplates();
        });
    };

    const handleSampleAlertSelect = (option: SampleAlertOption): void => {
        const clonedConfiguration = { ...option.alertConfiguration };
        clonedConfiguration.name +=
            "-" + Math.random().toString(36).substring(2, 5);

        createAlert(clonedConfiguration)
            .then((alert) => {
                if (navigateToAlertDetailAfterCreate) {
                    navigate(`${getAlertsAlertViewPath(alert.id)}`);
                } else {
                    const queryParams = new URLSearchParams([
                        [QUERY_PARAM_KEYS.SHOW_FIRST_ALERT_SUCCESS, "true"],
                    ]);
                    navigate(`${getHomePath()}?${queryParams.toString()}`);
                }
                setShowAppNavBar(true);
            })
            .catch((error: AxiosError): void => {
                const errMessages = getErrorMessages(error);

                notifyIfErrors(
                    ActionStatus.Error,
                    errMessages,
                    notify,
                    t("message.create-error", {
                        entity: t("label.sample-alert"),
                    })
                );
            });
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <Typography variant="h5">
                        {t("message.select-alert-type")}
                    </Typography>
                    <Typography variant="body1">
                        {t(
                            "message.this-is-the-detector-algorithm-that-will-rule-alert"
                        )}
                    </Typography>
                </Grid>
                {!hideSampleAlerts && !sampleAlertsBottom && (
                    <SampleAlertSelection
                        alertTemplates={alertTemplates}
                        onSampleAlertSelect={handleSampleAlertSelect}
                    />
                )}
                <Grid item xs={12}>
                    <EmptyStateSwitch
                        emptyState={
                            <PageContentsCardV1>
                                <Box padding={10}>
                                    <NoDataIndicator>
                                        <Box textAlign="center">
                                            {t(
                                                "message.in-order-to-continue-you-will-need-to-load"
                                            )}
                                        </Box>
                                        <Box marginTop={2} textAlign="center">
                                            <Button
                                                color="primary"
                                                onClick={
                                                    handleCreateDefaultAlertTemplates
                                                }
                                            >
                                                {t("label.load-defaults")}
                                            </Button>
                                        </Box>
                                    </NoDataIndicator>
                                </Box>
                            </PageContentsCardV1>
                        }
                        isEmpty={
                            filterOptionWithTemplateNames(advancedOptions)
                                .length === 0 &&
                            filterOptionWithTemplateNames(simpleOptions)
                                .length === 0
                        }
                    >
                        <AlgorithmSelection
                            advancedOptions={advancedOptions}
                            simpleOptions={simpleOptions}
                            onAlertPropertyChange={onAlertPropertyChange}
                            onSelectionComplete={handleAlgorithmSelection}
                        />
                    </EmptyStateSwitch>
                </Grid>

                {!hideSampleAlerts && sampleAlertsBottom && (
                    <SampleAlertSelection
                        alertTemplates={alertTemplates}
                        onSampleAlertSelect={handleSampleAlertSelect}
                    />
                )}
            </PageContentsGridV1>

            {!hideCurrentlySelected && selectedAlgorithmOption && (
                <WizardBottomBar
                    nextBtnLink={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`}
                    nextButtonLabel={t("label.continue-dont-change")}
                >
                    {t(
                        "message.algorithm-is-selected-for-current-configuration",
                        {
                            algorithmName:
                                selectedAlgorithmOption.algorithmOption.title,
                        }
                    )}
                </WizardBottomBar>
            )}
        </>
    );
};
