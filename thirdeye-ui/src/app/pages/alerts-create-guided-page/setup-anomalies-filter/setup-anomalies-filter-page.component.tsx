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
import { isEqual, reduce } from "lodash";
import {
    default as React,
    FunctionComponent,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    Link as RouterLink,
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { AnomaliesFilterPanel } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.component";
import { AnomaliesFilterConfiguratorRenderConfigs } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.interfaces";
import { getAvailableFilterOptions } from "../../../components/alert-wizard-v3/anomalies-filter-panel/anomalies-filter-panel.utils";
import { NavigateAlertCreationFlowsDropdown } from "../../../components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { ChartContent } from "../../../components/alert-wizard-v3/preview-chart/chart-content/chart-content.component";
import { PreviewChartHeader } from "../../../components/alert-wizard-v3/preview-chart/header/preview-chart-header.component";
import { EmptyStateSwitch } from "../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { Portal } from "../../../components/portal/portal.component";
import { TimeRangeQueryStringKey } from "../../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { PageContentsGridV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { createAlertEvaluation } from "../../../utils/alerts/alerts.util";
import {
    AppRouteRelative,
    generateDateRangeMonthsFromNow,
} from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";
import { useAlertsCreateGuidedPage } from "./setup-anomalies-filter-page.styles";

export const SetupAnomaliesFilterPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const classes = useAlertsCreateGuidedPage();
    const [searchParams, setSearchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    const {
        onAlertPropertyChange,
        alertTemplates,
        selectedAlgorithmOption,
        alert: alertConfigurationBeforeFilterChanges,
        alertInsight,
        getAlertInsightStatus,
        getAlertInsight,
    } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

    const {
        evaluation: evaluationWithoutFilters,
        status: getEvaluationWithoutFilterChangesStatus,
    } = useGetEvaluation();

    const {
        evaluation: evaluationWithFilters,
        getEvaluation: getEvaluationWithFilterChanges,
        status: getEvaluationWithFilterChangesStatus,
    } = useGetEvaluation();

    const beforeFilterChangesCount = useMemo(() => {
        if (evaluationWithoutFilters) {
            return reduce(
                evaluationWithoutFilters.detectionEvaluations,
                (soFar: number, evaluation) =>
                    soFar + evaluation.anomalies.length,
                0
            );
        }

        return null;
    }, [evaluationWithoutFilters]);

    const [
        alertConfigurationWithFilterChanges,
        setAlertConfigurationWithFilterChanges,
    ] = useState(alertConfigurationBeforeFilterChanges);
    const [isFilterPanelOpen, setIsFilterPanelOpen] = useState(false);
    const [evaluationTimeRange, setEvaluationTimeRange] = useState({
        startTime: startTime,
        endTime: endTime,
    });

    const selectedAlertTemplate = useMemo(() => {
        return alertTemplates.find((alertTemplate) => {
            /**
             * selectedAlgorithmOption could be undefined if users land
             * on filters page first (from edit flow)
             */
            return (
                alertTemplate.name ===
                selectedAlgorithmOption?.algorithmOption.alertTemplate
            );
        });
    }, [alertTemplates, selectedAlgorithmOption]);

    const availableConfigurations = useMemo(() => {
        if (!selectedAlertTemplate) {
            return undefined;
        }

        return getAvailableFilterOptions(selectedAlertTemplate, t);
    }, [selectedAlertTemplate]);

    const fetchAlertEvaluations = (start: number, end: number): void => {
        const copiedAlert = { ...alertConfigurationBeforeFilterChanges };
        delete copiedAlert.id;
        /* On the Preview Page we have to defer fetching the data for enumeration items till they
        are in view.
        We only fetch the list of enumeration items without data and anomalies
        by passing {listEnumerationItemsOnly: true} as fetching all the data at once introduces
        significant latency because of the request size.
        Hence we first fetch the evaluations with enumeration items without anomalies and data.
        And then enumerationRow component fetches anomalies and data progresivelly */
        const hasEnumerationItems =
            !!alertConfigurationBeforeFilterChanges.templateProperties
                ?.enumeratorQuery ||
            !!alertConfigurationBeforeFilterChanges.templateProperties
                ?.enumerationItems;

        const alertWithFilters = { ...alertConfigurationWithFilterChanges };
        delete alertWithFilters.id;
        getEvaluationWithFilterChanges(
            createAlertEvaluation(alertWithFilters, start, end, {
                listEnumerationItemsOnly: hasEnumerationItems,
            })
        );
        setEvaluationTimeRange({ startTime: start, endTime: end });
    };

    useEffect(() => {
        handleReloadPreviewClick();
    }, []);

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

    const anomalyCountForCurrentEvaluation = useMemo(() => {
        if (evaluationWithFilters) {
            return reduce(
                evaluationWithFilters?.detectionEvaluations,
                (soFar: number, evaluation) =>
                    soFar + evaluation.anomalies.length,
                0
            );
        }

        return null;
    }, [evaluationWithFilters]);

    const handleReloadPreviewClick = (): void => {
        if (
            getAlertInsightStatus === ActionStatus.Initial ||
            getAlertInsightStatus === ActionStatus.Error
        ) {
            getAlertInsight({
                alert: alertConfigurationBeforeFilterChanges,
            }).then((alertInsight) => {
                let [start, end] = generateDateRangeMonthsFromNow(3);

                if (alertInsight) {
                    start = alertInsight.defaultStartTime;
                    end = alertInsight.defaultEndTime;
                }

                fetchAlertEvaluations(start, end);
                searchParams.set(
                    TimeRangeQueryStringKey.START_TIME,
                    start.toString()
                );
                searchParams.set(
                    TimeRangeQueryStringKey.END_TIME,
                    end.toString()
                );

                setSearchParams(searchParams);
            });
            // If start or end is missing and there exists an alert insight
        } else if ((!startTime || !endTime) && alertInsight) {
            fetchAlertEvaluations(
                alertInsight.defaultStartTime,
                alertInsight.defaultEndTime
            );
        } else {
            fetchAlertEvaluations(startTime, endTime);
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
                <Grid item xs={12}>
                    <Card>
                        <CardContent>
                            <Grid
                                container
                                alignItems="center"
                                justifyContent="space-between"
                            >
                                <Grid item>
                                    <Typography variant="h5">
                                        <LoadingErrorStateSwitch
                                            errorState={<></>}
                                            isError={
                                                getEvaluationWithoutFilterChangesStatus ===
                                                ActionStatus.Error
                                            }
                                            isLoading={
                                                getEvaluationWithoutFilterChangesStatus ===
                                                    ActionStatus.Initial ||
                                                getEvaluationWithoutFilterChangesStatus ===
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
                                                        ({t("label.filtered")})
                                                    </Typography>
                                                )}
                                            <Typography variant="body2">
                                                {t(
                                                    "message.anomalies-are-detected-within-the-date-range"
                                                )}
                                            </Typography>
                                        </LoadingErrorStateSwitch>
                                    </Typography>
                                </Grid>
                                <Grid item>
                                    <Button
                                        color="primary"
                                        startIcon={<FilterListIcon />}
                                        onClick={() => {
                                            setIsFilterPanelOpen(true);
                                        }}
                                    >
                                        {t("label.filters-&-sensitivity")}
                                    </Button>
                                </Grid>
                            </Grid>

                            <Box padding={2}>
                                <Divider />
                            </Box>
                            <EmptyStateSwitch
                                emptyState={
                                    <>
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
                                                to={`../${
                                                    AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS
                                                }?${searchParams.toString()}`}
                                            >
                                                {t("label.next")}
                                            </Button>
                                        </Box>
                                    </>
                                }
                                isEmpty={
                                    availableConfigurations !== undefined &&
                                    availableConfigurations.length === 0
                                }
                            >
                                <Grid container>
                                    <Grid item xs={12}>
                                        <PreviewChartHeader
                                            alertInsight={alertInsight}
                                            getEvaluationStatus={
                                                getEvaluationWithFilterChangesStatus
                                            }
                                            showConfigurationNotReflective={
                                                !isEqual(
                                                    alertConfigurationWithFilterChanges,
                                                    alertConfigurationBeforeFilterChanges
                                                )
                                            }
                                            onReloadClick={
                                                handleReloadPreviewClick
                                            }
                                            onStartEndChange={(
                                                newStart,
                                                newEnd
                                            ) => {
                                                fetchAlertEvaluations(
                                                    newStart,
                                                    newEnd
                                                );
                                            }}
                                        />
                                    </Grid>
                                    <Grid item xs={12}>
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
                                                                to={`../${
                                                                    AppRouteRelative.WELCOME_CREATE_ALERT_TUNE_ALERT
                                                                }?${searchParams.toString()}`}
                                                            >
                                                                {t(
                                                                    "label.go-back"
                                                                )}
                                                            </Button>
                                                        </Grid>
                                                    </Grid>
                                                </Box>
                                            }
                                            isError={
                                                getEvaluationWithFilterChangesStatus ===
                                                ActionStatus.Error
                                            }
                                            isLoading={
                                                getEvaluationWithFilterChangesStatus ===
                                                    ActionStatus.Working ||
                                                getEvaluationWithFilterChangesStatus ===
                                                    ActionStatus.Initial
                                            }
                                        >
                                            <ChartContent
                                                hideCallToActionPrompt
                                                showLoadButton
                                                alert={
                                                    alertConfigurationWithFilterChanges
                                                }
                                                alertEvaluation={
                                                    evaluationWithFilters
                                                }
                                                evaluationTimeRange={
                                                    evaluationTimeRange
                                                }
                                                onAlertPropertyChange={
                                                    onAlertPropertyChange
                                                }
                                                onReloadClick={
                                                    handleReloadPreviewClick
                                                }
                                            />
                                        </LoadingErrorStateSwitch>
                                    </Grid>
                                </Grid>
                            </EmptyStateSwitch>
                        </CardContent>
                    </Card>
                </Grid>
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
                            onAlertPropertyChange={handleAlertPropertyChange}
                            onCloseClick={handleFilterPanelOnCloseClick}
                        />
                    </Paper>
                </Portal>
            )}

            <WizardBottomBar
                backBtnLink={`../${
                    AppRouteRelative.WELCOME_CREATE_ALERT_TUNE_ALERT
                }?${searchParams.toString()}`}
                handleNextClick={() => {
                    onAlertPropertyChange({
                        templateProperties:
                            alertConfigurationWithFilterChanges.templateProperties,
                    });
                    navigate(
                        `../${
                            AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS
                        }?${searchParams.toString()}`
                    );
                }}
                nextButtonLabel={t("label.next")}
            >
                <Button
                    component={RouterLink}
                    to={`../${
                        AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS
                    }?${searchParams.toString()}`}
                >
                    {t("label.skip")}
                </Button>
            </WizardBottomBar>
        </>
    );
};
