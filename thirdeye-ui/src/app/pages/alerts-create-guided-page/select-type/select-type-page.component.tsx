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
import {
    default as React,
    FunctionComponent,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { AlertTemplatesInformationLinks } from "../../../components/alert-wizard-v3/alert-templates-information-links/alert-templates-information-links";
import { AlertTypeSelection } from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.component";
import { filterOptionWithTemplateNames } from "../../../components/alert-wizard-v3/alert-type-selection/alert-type-selection.utils";
import { NavigateAlertCreationFlowsDropdown } from "../../../components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { NoDataIndicator } from "../../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    SkeletonV1,
} from "../../../platform/components";
import { createDefaultAlertTemplates } from "../../../rest/alert-templates/alert-templates.rest";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";

const SKELETON_HEIGHT = 300;

export const SelectTypePage: FunctionComponent = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const {
        onAlertPropertyChange,
        alertTypeOptions,
        getAlertTemplates,
        alertTemplates,
        selectedAlgorithmOption,
        alert,
        isMultiDimensionAlert,

        alertRecommendations,
        getAlertRecommendationIsLoading,
    } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

    const [isLoading, setIsLoading] = useState(getAlertRecommendationIsLoading);

    const firstRecommendedAlertTemplate = useMemo(() => {
        if (alertRecommendations && alertRecommendations.length > 0) {
            return alertRecommendations[0]?.alert.template?.name;
        }

        return undefined;
    }, [alertRecommendations]);

    // At most wait 2 seconds for the alert recommendation api to complete
    useEffect(() => {
        if (getAlertRecommendationIsLoading) {
            const timer = setTimeout(() => {
                setIsLoading(false);
            }, 2000);

            return () => clearTimeout(timer);
        } else {
            setIsLoading(false);

            return () => null;
        }
    }, []);

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
                                {t(
                                    "message.select-the-algorithm-type-best-fit-for-your-alert"
                                )}
                            </Typography>
                            <AlertTemplatesInformationLinks />
                        </Grid>
                        <Grid item>
                            <NavigateAlertCreationFlowsDropdown />
                        </Grid>
                    </Grid>
                </Grid>

                <Grid item xs={12}>
                    <LoadingErrorStateSwitch
                        isError={false}
                        isLoading={isLoading}
                        loadingState={
                            <Grid container spacing={2}>
                                {[...Array(4)].map((idx) => {
                                    return (
                                        <Grid
                                            item
                                            key={idx}
                                            md={6}
                                            sm={12}
                                            xs={12}
                                        >
                                            <PageContentsCardV1>
                                                <SkeletonV1
                                                    height={SKELETON_HEIGHT}
                                                    variant="rect"
                                                />
                                            </PageContentsCardV1>
                                        </Grid>
                                    );
                                })}
                            </Grid>
                        }
                    >
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
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
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
                                filterOptionWithTemplateNames(
                                    alertTypeOptions,
                                    isMultiDimensionAlert
                                ).length === 0
                            }
                        >
                            <AlertTypeSelection
                                alertTemplates={alertTemplates}
                                isMultiDimensionAlert={isMultiDimensionAlert}
                                recommendedAlertTemplate={
                                    firstRecommendedAlertTemplate
                                }
                                selectedAlertTemplateName={alert.template?.name}
                                onAlertPropertyChange={onAlertPropertyChange}
                            />
                        </EmptyStateSwitch>
                    </LoadingErrorStateSwitch>
                </Grid>
            </PageContentsGridV1>

            {selectedAlgorithmOption && (
                <WizardBottomBar
                    backBtnLink={`../${
                        AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_METRIC
                    }?${searchParams.toString()}`}
                    nextBtnLink={`../${
                        AppRouteRelative.WELCOME_CREATE_ALERT_TUNE_ALERT
                    }?${searchParams.toString()}`}
                />
            )}
        </>
    );
};
