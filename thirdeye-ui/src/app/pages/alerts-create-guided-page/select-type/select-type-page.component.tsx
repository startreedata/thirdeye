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
import { Box, Button, Grid, Typography } from "@material-ui/core";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import {
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { AlertTypeSelection } from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.component";
import { filterOptionWithTemplateNames } from "../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.utils";
import { NavigateAlertCreationFlowsDropdown } from "../../../components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { NoDataIndicator } from "../../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../platform/components";
import { createDefaultAlertTemplates } from "../../../rest/alert-templates/alert-templates.rest";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";
import { SelectTypePageProps } from "./select-type-page.interface";

export const SelectTypePage: FunctionComponent<SelectTypePageProps> = ({
    hideCurrentlySelected,
}) => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const {
        onAlertPropertyChange,
        simpleOptions,
        advancedOptions,
        getAlertTemplates,
        alertTemplates,
        selectedAlgorithmOption,
        alert,
    } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

    useEffect(() => {
        // On initial render, ensure metric is selected
        const newAlert = createNewStartingAlert();

        const metricIsSelected =
            newAlert.templateProperties.dataset !==
                alert.templateProperties.dataset &&
            newAlert.templateProperties.aggregationColumn !==
                alert.templateProperties.aggregationColumn;

        if (!metricIsSelected) {
            navigate(
                `../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_METRIC}`
            );
        }
    }, []);

    const handleAlgorithmSelection = (): void => {
        navigate(
            `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`
        );
    };

    const handleCreateDefaultAlertTemplates = (): void => {
        createDefaultAlertTemplates().then(() => {
            getAlertTemplates();
        });
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <Grid
                        container
                        alignContent="center"
                        justifyContent="space-between"
                    >
                        <Grid item>
                            <Typography variant="h5">
                                {t("message.select-alert-type")}
                            </Typography>
                            <Typography variant="body1">
                                {t(
                                    "message.this-is-the-detector-algorithm-that-will-rule-alert"
                                )}
                            </Typography>
                        </Grid>
                        <Grid item>
                            <NavigateAlertCreationFlowsDropdown />
                        </Grid>
                    </Grid>
                </Grid>

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
                        <AlertTypeSelection
                            alertTemplates={alertTemplates}
                            onAlertPropertyChange={onAlertPropertyChange}
                            onSelectionComplete={handleAlgorithmSelection}
                        />
                    </EmptyStateSwitch>
                </Grid>
            </PageContentsGridV1>

            {!hideCurrentlySelected && selectedAlgorithmOption && (
                <WizardBottomBar
                    backBtnLink={`../${
                        AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_METRIC
                    }?${searchParams.toString()}`}
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
