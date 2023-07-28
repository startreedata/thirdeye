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
import { Box, Divider, Grid, Typography } from "@material-ui/core";
import {
    default as React,
    FunctionComponent,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { NavigateAlertCreationFlowsDropdown } from "../../../components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { ChartContent } from "../../../components/alert-wizard-v3/preview-chart/chart-content/chart-content.component";
import { PreviewChartHeader } from "../../../components/alert-wizard-v3/preview-chart/header/preview-chart-header.component";
import { SelectMetric } from "../../../components/alert-wizard-v3/select-metric/select-metric.component";
import { NoDataIndicator } from "../../../components/no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeQueryStringKey } from "../../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { createAlertEvaluation } from "../../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import {
    AppRouteRelative,
    generateDateRangeMonthsFromNow,
} from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";

const ALERT_TEMPLATE_FOR_EVALUATE = "startree-threshold";
const ALERT_TEMPLATE_FOR_EVALUATE_DX = "startree-threshold-dx";

const PROPERTIES_TO_COPY = [
    "dataSource",
    "dataset",
    "aggregationColumn",
    "aggregationFunction",
    "monitoringGranularity",
    "enumerationItems",
    "queryFilters",
];

export const SetupMetricPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    const {
        alert,
        onAlertPropertyChange,
        selectedAlgorithmOption,
        alertTemplates,
        alertInsight,
        getAlertInsight,
        getAlertInsightStatus,
        isMultiDimensionAlert,

        getAlertRecommendation,
    } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

    const {
        evaluation,
        getEvaluation,
        errorMessages: getEvaluationRequestErrors,
        status: getEvaluationStatus,
    } = useGetEvaluation();

    useEffect(() => {
        notifyIfErrors(
            getEvaluationStatus,
            getEvaluationRequestErrors,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.chart-data"),
            })
        );
    }, [getEvaluationStatus]);

    const alertTemplateForEvaluate = useMemo(() => {
        const alertTemplateToFind = isMultiDimensionAlert
            ? ALERT_TEMPLATE_FOR_EVALUATE_DX
            : ALERT_TEMPLATE_FOR_EVALUATE;

        return alertTemplates.find((alertTemplateCandidate) => {
            return alertTemplateCandidate.name === alertTemplateToFind;
        });
    }, [alertTemplates, alert, isMultiDimensionAlert]);

    const [alertConfigForPreview, setAlertConfigForPreview] =
        useState<EditableAlert>(() => {
            const workingAlert = createNewStartingAlert();

            PROPERTIES_TO_COPY.forEach((propKey) => {
                if (alert.templateProperties[propKey]) {
                    workingAlert.templateProperties[propKey] =
                        alert.templateProperties[propKey];
                }
            });

            if (workingAlert.template) {
                workingAlert.template.name = alertTemplateForEvaluate?.name;
            }

            workingAlert.templateProperties.min = 0;
            workingAlert.templateProperties.max = 0;

            return workingAlert;
        });

    const shouldShowLoadButton = useMemo(() => {
        const newAlert = createNewStartingAlert();

        return (
            newAlert.templateProperties.dataset !==
                alertConfigForPreview.templateProperties.dataset &&
            newAlert.templateProperties.aggregationColumn !==
                alertConfigForPreview.templateProperties.aggregationColumn
        );
    }, [alertConfigForPreview]);

    const handleAlertPropertyChange = (
        newConfiguration: Partial<EditableAlert>
    ): void => {
        onAlertPropertyChange(newConfiguration);

        setAlertConfigForPreview((currentConfig) => {
            const copied = {
                ...currentConfig,
            };

            if (newConfiguration.templateProperties) {
                copied.templateProperties = newConfiguration.templateProperties;

                copied.templateProperties.min = 0;
                copied.templateProperties.max = 0;
            }

            getAlertRecommendation(copied);

            return copied;
        });
    };

    const fetchAlertEvaluation = (start: number, end: number): void => {
        const copiedAlert = { ...alertConfigForPreview };
        delete copiedAlert.id;
        getEvaluation(createAlertEvaluation(copiedAlert, start, end));
    };

    const handleReloadPreviewClick = (): void => {
        if (
            getAlertInsightStatus === ActionStatus.Initial ||
            getAlertInsightStatus === ActionStatus.Error
        ) {
            getAlertInsight({ alert: alertConfigForPreview }).then(
                (alertInsight) => {
                    let [start, end] = generateDateRangeMonthsFromNow(3);

                    if (alertInsight) {
                        start = alertInsight.defaultStartTime;
                        end = alertInsight.defaultEndTime;
                    }

                    fetchAlertEvaluation(start, end);
                    searchParams.set(
                        TimeRangeQueryStringKey.START_TIME,
                        start.toString()
                    );
                    searchParams.set(
                        TimeRangeQueryStringKey.END_TIME,
                        end.toString()
                    );

                    setSearchParams(searchParams);
                }
            );
        } else if ((!startTime || !endTime) && alertInsight) {
            // If start or end is missing and there exists an alert insight
            fetchAlertEvaluation(
                alertInsight.defaultStartTime,
                alertInsight.defaultEndTime
            );
        } else {
            fetchAlertEvaluation(startTime, endTime);
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
                                {t("label.alert-setup")}
                            </Typography>
                            <Typography variant="body1">
                                {t("message.alert-setup-description")}
                            </Typography>
                        </Grid>
                        <Grid item>
                            <NavigateAlertCreationFlowsDropdown />
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <SelectMetric
                            alert={alert}
                            algorithmOptionConfig={selectedAlgorithmOption}
                            onAlertPropertyChange={handleAlertPropertyChange}
                        />

                        <Grid item xs={12}>
                            <Box marginBottom={2} marginTop={2} padding={1}>
                                <Divider />
                            </Box>
                        </Grid>

                        <Grid item xs={12}>
                            <PreviewChartHeader
                                alertInsight={alertInsight}
                                getEvaluationStatus={getEvaluationStatus}
                                onReloadClick={handleReloadPreviewClick}
                                onStartEndChange={(newStart, newEnd) => {
                                    fetchAlertEvaluation(newStart, newEnd);
                                }}
                            />
                        </Grid>

                        <Grid item xs={12}>
                            <LoadingErrorStateSwitch
                                errorState={
                                    <Box padding={15} paddingTop={15}>
                                        <NoDataIndicator>
                                            {t(
                                                "message.experienced-error-while-fetching-chart-data-try"
                                            )}
                                        </NoDataIndicator>
                                    </Box>
                                }
                                isError={
                                    getEvaluationStatus === ActionStatus.Error
                                }
                                isLoading={
                                    getEvaluationStatus ===
                                        ActionStatus.Working ||
                                    getAlertInsightStatus ===
                                        ActionStatus.Working
                                }
                                loadingState={
                                    <Box paddingTop={1}>
                                        <SkeletonV1
                                            animation="pulse"
                                            height={300}
                                            variant="rect"
                                        />
                                    </Box>
                                }
                            >
                                <ChartContent
                                    showOnlyActivity
                                    alert={alert}
                                    alertEvaluation={evaluation}
                                    hideCallToActionPrompt={
                                        shouldShowLoadButton
                                    }
                                    showLoadButton={shouldShowLoadButton}
                                    onAlertPropertyChange={
                                        onAlertPropertyChange
                                    }
                                    onReloadClick={handleReloadPreviewClick}
                                />
                            </LoadingErrorStateSwitch>
                        </Grid>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            <WizardBottomBar
                backBtnLink={
                    isMultiDimensionAlert
                        ? `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION}`
                        : "../"
                }
                nextBtnLink={`../${
                    AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE
                }?${searchParams.toString()}`}
                nextButtonIsDisabled={!shouldShowLoadButton}
            />
        </>
    );
};
