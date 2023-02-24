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
import { Box, Button, Grid } from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import { Alert } from "@material-ui/lab";
import { isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { ReactComponent as ChartSkeleton } from "../../../../assets/images/chart-skeleton.svg";
import {
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    useGetAlertInsight,
    useGetEvaluation,
} from "../../../rest/alerts/alerts.actions";
import {
    AlertEvaluation,
    AlertInEvaluation,
    EditableAlert,
    EnumerationItemConfig,
} from "../../../rest/dto/alert.interfaces";
import { DetectionEvaluation } from "../../../rest/dto/detection.interfaces";
import {
    createAlertEvaluation,
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
} from "../../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2/alert-wizard-v2.styles";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { generateChartOptionsForAlert } from "../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { EnumerationItemsTable } from "./enumeration-items-table/enumeration-items-table.component";
import { PreviewChartProps } from "./preview-chart.interfaces";
import { usePreviewChartStyles } from "./preview-chart.styles";

export const PreviewChart: FunctionComponent<PreviewChartProps> = ({
    alert,
    showLoadButton,
    onAlertPropertyChange,
    fetchOnInitialRender,
    headerComponent,
    onEvaluationFetchStart,
}) => {
    const sharedWizardClasses = useAlertWizardV2Styles();
    const previewChartClasses = usePreviewChartStyles();
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );
    const { notify } = useNotificationProviderV1();

    const {
        evaluation,
        getEvaluation,
        errorMessages: getEvaluationRequestErrors,
        status: getEvaluationStatus,
    } = useGetEvaluation();

    const {
        alertInsight,
        getAlertInsight,
        status: getAlertInsightStatus,
    } = useGetAlertInsight();

    const [detectionEvaluations, setDetectionEvaluations] =
        useState<DetectionEvaluation[]>();
    const [alertForCurrentEvaluation, setAlertForCurrentEvaluation] =
        useState<EditableAlert>();

    const fetchAlertEvaluation = async (
        start: number,
        end: number
    ): Promise<void> => {
        onEvaluationFetchStart && onEvaluationFetchStart(start, end);

        const copiedAlert = { ...alert };
        delete copiedAlert.id;
        const fetchedAlertEvaluation = await getEvaluation(
            createAlertEvaluation(copiedAlert, start, end)
        );

        setAlertForCurrentEvaluation(alert);

        if (fetchedAlertEvaluation === undefined) {
            setDetectionEvaluations(undefined);
        }

        const evaluations = extractDetectionEvaluation(
            fetchedAlertEvaluation as AlertEvaluation
        );

        setDetectionEvaluations(evaluations);
    };

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

    const firstTimeSeriesOptions = useMemo(() => {
        if (!detectionEvaluations || detectionEvaluations.length === 0) {
            return null;
        }

        const timeseriesConfiguration = generateChartOptionsForAlert(
            detectionEvaluations[0],
            detectionEvaluations[0].anomalies,
            t,
            undefined,
            determineTimezoneFromAlertInEvaluation(
                evaluation?.alert.template as Pick<
                    AlertInEvaluation,
                    "metadata"
                >
            )
        );

        timeseriesConfiguration.brush = false;
        timeseriesConfiguration.zoom = true;
        timeseriesConfiguration.svgContainerUseAuto = true;

        return timeseriesConfiguration;
    }, [detectionEvaluations]);

    const handleAutoRangeClick = (): void => {
        getAlertInsight({ alert });
    };

    useEffect(() => {
        if (
            getAlertInsightStatus === ActionStatus.Done ||
            getAlertInsightStatus === ActionStatus.Error
        ) {
            let startToUse = startTime;
            let endToUse = endTime;

            if (alertInsight) {
                searchParams.set(
                    TimeRangeQueryStringKey.START_TIME,
                    alertInsight.defaultStartTime.toString()
                );
                searchParams.set(
                    TimeRangeQueryStringKey.END_TIME,
                    alertInsight.defaultEndTime.toString()
                );
                setSearchParams(searchParams, { replace: true });

                startToUse = alertInsight.defaultStartTime;
                endToUse = alertInsight.defaultEndTime;
            }

            fetchAlertEvaluation(startToUse, endToUse);
        }
    }, [getAlertInsightStatus]);

    const handleDeleteEnumerationItemClick = (
        detectionEvaluation: DetectionEvaluation
    ): void => {
        const currentEnumerations: EnumerationItemConfig[] = alert
            .templateProperties.enumerationItems as EnumerationItemConfig[];
        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                enumerationItems: currentEnumerations.filter((c) => {
                    return !isEqual(
                        c.params,
                        detectionEvaluation?.enumerationItem?.params
                    );
                }),
            },
        });

        detectionEvaluations &&
            setDetectionEvaluations(
                detectionEvaluations.filter((c) => {
                    return !isEqual(
                        c.enumerationItem,
                        detectionEvaluation?.enumerationItem
                    );
                })
            );
    };

    useEffect(() => {
        if (fetchOnInitialRender) {
            handleAutoRangeClick();
        }
    }, []);

    return (
        <>
            {headerComponent &&
                headerComponent(evaluation, getEvaluationStatus)}
            {getEvaluationStatus !== ActionStatus.Initial && (
                <>
                    <Grid item xs={12}>
                        <Grid
                            container
                            alignItems="center"
                            justifyContent="space-between"
                        >
                            <Grid item>
                                <Button
                                    color="primary"
                                    disabled={!showLoadButton}
                                    variant="outlined"
                                    onClick={() => {
                                        const isNotInitialRequest =
                                            (getEvaluationStatus as ActionStatus) !==
                                            ActionStatus.Initial;
                                        const missingStartEnd =
                                            !startTime || !endTime;

                                        if (
                                            isNotInitialRequest &&
                                            !missingStartEnd
                                        ) {
                                            fetchAlertEvaluation(
                                                startTime,
                                                endTime
                                            );
                                        } else {
                                            handleAutoRangeClick();
                                        }
                                    }}
                                >
                                    <RefreshIcon fontSize="small" />
                                    {t("label.reload-preview")}
                                </Button>
                            </Grid>
                            <Grid item>
                                <TimeRangeButtonWithContext
                                    hideQuickExtend
                                    btnGroupColor="primary"
                                    timezone={determineTimezoneFromAlertInEvaluation(
                                        evaluation?.alert.template as Pick<
                                            AlertInEvaluation,
                                            "metadata"
                                        >
                                    )}
                                    onTimeRangeChange={(start, end) =>
                                        fetchAlertEvaluation(start, end)
                                    }
                                />
                            </Grid>
                        </Grid>
                    </Grid>
                    {!isEqual(alertForCurrentEvaluation, alert) &&
                        (getEvaluationStatus as ActionStatus) !==
                            ActionStatus.Initial &&
                        getEvaluationStatus !== ActionStatus.Working && (
                            <Grid item xs={12}>
                                <Box paddingTop={1}>
                                    <Alert
                                        severity="warning"
                                        variant="outlined"
                                    >
                                        {t(
                                            "message.chart-data-not-reflective-of-current-config"
                                        )}
                                    </Alert>
                                </Box>
                            </Grid>
                        )}
                </>
            )}

            <Grid item xs={12}>
                <Box marginTop={1} minHeight={100} position="relative">
                    <LoadingErrorStateSwitch
                        errorState={
                            <Box padding={15}>
                                <NoDataIndicator />
                            </Box>
                        }
                        isError={getEvaluationStatus === ActionStatus.Error}
                        isLoading={
                            getEvaluationStatus === ActionStatus.Working ||
                            getAlertInsightStatus === ActionStatus.Working
                        }
                        loadingState={
                            <SkeletonV1
                                animation="pulse"
                                height={300}
                                variant="rect"
                            />
                        }
                    >
                        {!detectionEvaluations && (
                            <Box marginTop={1} position="relative">
                                <Box
                                    className={
                                        previewChartClasses.alertContainer
                                    }
                                >
                                    <Box position="absolute" width="100%">
                                        <Grid
                                            container
                                            justifyContent="space-around"
                                        >
                                            <Grid item>
                                                <Alert
                                                    className={
                                                        sharedWizardClasses.infoAlert
                                                    }
                                                    severity="info"
                                                >
                                                    {t(
                                                        "message.please-complete-the-missing-information-to-see-preview"
                                                    )}
                                                </Alert>
                                            </Grid>
                                        </Grid>
                                    </Box>
                                    <Grid
                                        container
                                        alignItems="center"
                                        className={
                                            previewChartClasses.heightWholeContainer
                                        }
                                        justifyContent="space-around"
                                    >
                                        <Grid item>
                                            <Button
                                                color="primary"
                                                disabled={!showLoadButton}
                                                variant="text"
                                                onClick={handleAutoRangeClick}
                                            >
                                                <RefreshIcon fontSize="large" />
                                                {t("label.load-chart")}
                                            </Button>
                                        </Grid>
                                    </Grid>
                                </Box>
                                <Box width="100%">
                                    <ChartSkeleton />
                                </Box>
                            </Box>
                        )}

                        {detectionEvaluations?.length === 1 &&
                            firstTimeSeriesOptions && (
                                <Box marginTop={1}>
                                    <TimeSeriesChart
                                        height={300}
                                        {...firstTimeSeriesOptions}
                                    />
                                </Box>
                            )}

                        {detectionEvaluations &&
                            detectionEvaluations.length > 1 && (
                                <Box marginTop={1}>
                                    <EnumerationItemsTable
                                        detectionEvaluations={
                                            detectionEvaluations
                                        }
                                        timezone={determineTimezoneFromAlertInEvaluation(
                                            evaluation?.alert.template as Pick<
                                                AlertInEvaluation,
                                                "metadata"
                                            >
                                        )}
                                        onDeleteClick={
                                            handleDeleteEnumerationItemClick
                                        }
                                    />
                                </Box>
                            )}
                    </LoadingErrorStateSwitch>
                </Box>
            </Grid>
        </>
    );
};
