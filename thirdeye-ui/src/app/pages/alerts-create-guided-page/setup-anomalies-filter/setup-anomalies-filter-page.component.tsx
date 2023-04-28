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
import {
    Box,
    Button,
    Card,
    CardContent,
    Divider,
    Grid,
    Paper,
    Typography,
} from "@material-ui/core";
import FilterListIcon from "@material-ui/icons/FilterList";
import { Skeleton } from "@material-ui/lab";
import { reduce } from "lodash";
import { default as React, FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    Link as RouterLink,
    useNavigate,
    useOutletContext,
} from "react-router-dom";
import { AnomaliesFilterPanel } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.component";
import { AnomaliesFilterConfiguratorRenderConfigs } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.interfaces";
import { getAvailableFilterOptions } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.utils";
import { NavigateAlertCreationFlowsDropdown } from "../../../components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { PreviewChart } from "../../../components/alert-wizard-v3/preview-chart/preview-chart.component";
import { EmptyStateSwitch } from "../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { Portal } from "../../../components/portal/portal.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { PageContentsGridV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { createAlertEvaluation } from "../../../utils/alerts/alerts.util";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";
import { SetupAnomaliesFilterPageProps } from "./setup-anomalies-filter-page.interface";
import { useAlertsCreateGuidedPage } from "./setup-anomalies-filter-page.styles";

export const SetupAnomaliesFilterPage: FunctionComponent<SetupAnomaliesFilterPageProps> =
    ({ hideCurrentlySelected }) => {
        const { t } = useTranslation();
        const navigate = useNavigate();
        const classes = useAlertsCreateGuidedPage();

        const {
            onAlertPropertyChange,
            alertTemplates,
            selectedAlgorithmOption,
            alert: alertConfigurationBeforeFilterChanges,
        } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

        const {
            evaluation: evaluationForConfigurationBeforeFilterChanges,
            getEvaluation,
            status: getEvaluationStatusForConfigurationBeforeFilterChanges,
        } = useGetEvaluation();

        const beforeFilterChangesCount = useMemo(() => {
            if (evaluationForConfigurationBeforeFilterChanges) {
                return reduce(
                    evaluationForConfigurationBeforeFilterChanges.detectionEvaluations,
                    (soFar: number, evaluation) =>
                        soFar + evaluation.anomalies.length,
                    0
                );
            }

            return null;
        }, [evaluationForConfigurationBeforeFilterChanges]);

        const [
            alertConfigurationWithFilterChanges,
            setAlertConfigurationWithFilterChanges,
        ] = useState(alertConfigurationBeforeFilterChanges);
        const [isFilterPanelOpen, setIsFilterPanelOpen] = useState(false);

        const selectedAlertTemplate = useMemo(() => {
            return alertTemplates.find((alertTemplate) => {
                return (
                    alertTemplate.name ===
                    selectedAlgorithmOption.algorithmOption.alertTemplate
                );
            });
        }, [alertTemplates, selectedAlgorithmOption]);

        const availableConfigurations = useMemo(() => {
            if (!selectedAlertTemplate) {
                return undefined;
            }

            return getAvailableFilterOptions(selectedAlertTemplate, t);
        }, [selectedAlertTemplate]);

        const fetchAlertEvaluation = (start: number, end: number): void => {
            const copiedAlert = { ...alertConfigurationBeforeFilterChanges };
            delete copiedAlert.id;
            getEvaluation(createAlertEvaluation(copiedAlert, start, end));
        };

        const handleFilterPanelOnCloseClick = (): void => {
            setIsFilterPanelOpen(false);
        };

        const handleAlertPropertyChange = (
            contentsToReplace: Partial<EditableAlert>,
            isTotalReplace = false
        ): void => {
            if (isTotalReplace) {
                setAlertConfigurationWithFilterChanges(
                    contentsToReplace as EditableAlert
                );
            } else {
                setAlertConfigurationWithFilterChanges((currentAlert) => {
                    return {
                        ...currentAlert,
                        ...contentsToReplace,
                    } as EditableAlert;
                });
            }
        };

        return (
            <>
                <PageContentsGridV1>
                    <Grid item xs={12}>
                        <Grid
                            container
                            alignItems="center"
                            justifyContent="space-between"
                        >
                            <Grid item>
                                <Typography variant="h5">
                                    {t(
                                        "message.configure-anomaly-filters-and-sensitivity"
                                    )}
                                </Typography>
                                <Typography variant="body1">
                                    {t(
                                        "message.configure-anomaly-filters-and-adjust-sensitivity"
                                    )}
                                </Typography>
                            </Grid>
                            <Grid item>
                                <NavigateAlertCreationFlowsDropdown />
                            </Grid>
                        </Grid>
                    </Grid>
                    <EmptyStateSwitch
                        emptyState={
                            <Grid item xs={12}>
                                <Card>
                                    <CardContent>
                                        <Box padding={5} textAlign="center">
                                            {t(
                                                "message.there-are-no-filters-available-please-continue"
                                            )}
                                        </Box>
                                        <Box
                                            paddingBottom={5}
                                            textAlign="center"
                                        >
                                            <Button
                                                color="primary"
                                                component={RouterLink}
                                                to={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS}`}
                                            >
                                                {t("label.next")}
                                            </Button>
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>
                        }
                        isEmpty={
                            availableConfigurations !== undefined &&
                            availableConfigurations.length === 0
                        }
                    >
                        <Grid item xs={12}>
                            <Card>
                                <CardContent>
                                    <LoadingErrorStateSwitch
                                        errorState={
                                            <Box textAlign="center">
                                                <Grid container>
                                                    <Grid item xs={12}>
                                                        <Typography color="error">
                                                            {t(
                                                                "message.experienced-an-issue-fetching-chart-data"
                                                            )}
                                                        </Typography>
                                                    </Grid>
                                                    <Grid item xs={12}>
                                                        {t(
                                                            "message.please-go-back-and-review-the-alert-configuration"
                                                        )}
                                                    </Grid>
                                                    <Grid item xs={12}>
                                                        <Button
                                                            component={
                                                                RouterLink
                                                            }
                                                            to={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`}
                                                        >
                                                            {t("label.go-back")}
                                                        </Button>
                                                    </Grid>
                                                </Grid>
                                            </Box>
                                        }
                                        isError={
                                            getEvaluationStatusForConfigurationBeforeFilterChanges ===
                                            ActionStatus.Error
                                        }
                                        isLoading={false}
                                    >
                                        <PreviewChart
                                            fetchOnInitialRender
                                            showLoadButton
                                            alert={
                                                alertConfigurationWithFilterChanges
                                            }
                                            headerComponent={(
                                                evaluation,
                                                evaluationRequestStatus
                                            ) => {
                                                const anomalyCountForCurrentEvaluation =
                                                    evaluation
                                                        ? reduce(
                                                              evaluation?.detectionEvaluations,
                                                              (
                                                                  soFar: number,
                                                                  evaluation
                                                              ) =>
                                                                  soFar +
                                                                  evaluation
                                                                      .anomalies
                                                                      .length,
                                                              0
                                                          )
                                                        : null;

                                                return (
                                                    <>
                                                        <Grid
                                                            container
                                                            alignItems="center"
                                                            justifyContent="space-between"
                                                        >
                                                            <Grid item>
                                                                <Typography variant="h5">
                                                                    <LoadingErrorStateSwitch
                                                                        isError={
                                                                            evaluationRequestStatus ===
                                                                            ActionStatus.Error
                                                                        }
                                                                        isLoading={
                                                                            evaluationRequestStatus ===
                                                                                ActionStatus.Initial ||
                                                                            evaluationRequestStatus ===
                                                                                ActionStatus.Working
                                                                        }
                                                                        loadingState={
                                                                            <Skeleton variant="text" />
                                                                        }
                                                                    >
                                                                        {anomalyCountForCurrentEvaluation !==
                                                                            null &&
                                                                            t(
                                                                                "message.total-anomalies-detected",
                                                                                {
                                                                                    num: anomalyCountForCurrentEvaluation,
                                                                                }
                                                                            )}
                                                                        {beforeFilterChangesCount !==
                                                                            null &&
                                                                            beforeFilterChangesCount !==
                                                                                anomalyCountForCurrentEvaluation && (
                                                                                <Typography
                                                                                    color="textSecondary"
                                                                                    variant="inherit"
                                                                                >
                                                                                    {" "}
                                                                                    (
                                                                                    {t(
                                                                                        "label.filtered"
                                                                                    )}

                                                                                    )
                                                                                </Typography>
                                                                            )}
                                                                    </LoadingErrorStateSwitch>
                                                                </Typography>
                                                                <Typography variant="body2">
                                                                    {t(
                                                                        "message.anomalies-are-detected-within-the-date-range"
                                                                    )}
                                                                </Typography>
                                                            </Grid>
                                                            <Grid item>
                                                                <Button
                                                                    color="primary"
                                                                    startIcon={
                                                                        <FilterListIcon />
                                                                    }
                                                                    onClick={() => {
                                                                        setIsFilterPanelOpen(
                                                                            true
                                                                        );
                                                                    }}
                                                                >
                                                                    {t(
                                                                        "label.filters-&-sensitivity"
                                                                    )}
                                                                </Button>
                                                            </Grid>
                                                        </Grid>

                                                        <Box padding={2}>
                                                            <Divider />
                                                        </Box>
                                                    </>
                                                );
                                            }}
                                            onAlertPropertyChange={
                                                onAlertPropertyChange
                                            }
                                            onEvaluationFetchStart={(
                                                start,
                                                end
                                            ) => {
                                                fetchAlertEvaluation(
                                                    start,
                                                    end
                                                );
                                            }}
                                        />
                                    </LoadingErrorStateSwitch>
                                </CardContent>
                            </Card>
                        </Grid>
                    </EmptyStateSwitch>
                </PageContentsGridV1>

                {selectedAlertTemplate && isFilterPanelOpen && (
                    <Portal containerId="guided-user-flow-portal">
                        <Paper
                            className={classes.filterPanelContainer}
                            elevation={3}
                        >
                            <AnomaliesFilterPanel
                                alert={alertConfigurationWithFilterChanges}
                                alertTemplate={selectedAlertTemplate}
                                availableConfigurations={
                                    availableConfigurations as AnomaliesFilterConfiguratorRenderConfigs[]
                                }
                                onAlertPropertyChange={
                                    handleAlertPropertyChange
                                }
                                onCloseClick={handleFilterPanelOnCloseClick}
                            />
                        </Paper>
                    </Portal>
                )}

                {!hideCurrentlySelected && selectedAlgorithmOption && (
                    <WizardBottomBar
                        backBtnLink={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`}
                        handleNextClick={() => {
                            onAlertPropertyChange({
                                templateProperties:
                                    alertConfigurationWithFilterChanges.templateProperties,
                            });
                            navigate(
                                `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS}`
                            );
                        }}
                        nextButtonLabel={t("label.next")}
                    >
                        <Button
                            component={RouterLink}
                            to={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS}`}
                        >
                            {t("label.skip")}
                        </Button>
                    </WizardBottomBar>
                )}
            </>
        );
    };
