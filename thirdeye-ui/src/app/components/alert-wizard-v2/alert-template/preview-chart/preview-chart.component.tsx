/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Box,
    Button,
    FormControl,
    Grid,
    MenuItem,
    Select,
    Typography,
} from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import { Alert } from "@material-ui/lab";
import { isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { ReactComponent as ChartSkeleton } from "../../../../../assets/images/chart-skeleton.svg";
import {
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import {
    useGetAlertInsight,
    useGetEvaluation,
} from "../../../../rest/alerts/alerts.actions";
import {
    AlertEvaluation,
    EditableAlert,
} from "../../../../rest/dto/alert.interfaces";
import { DetectionEvaluation } from "../../../../rest/dto/detection.interfaces";
import {
    createAlertEvaluation,
    extractDetectionEvaluation,
} from "../../../../utils/alerts/alerts.util";
import { generateNameForDetectionResult } from "../../../../utils/enumeration-items/enumeration-items.util";
import { notifyIfErrors } from "../../../../utils/notifications/notifications.util";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { generateChartOptionsForAlert } from "../../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeButtonWithContext } from "../../../time-range/time-range-button-with-context/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../../visualizations/time-series-chart/time-series-chart.component";
import { TimeSeriesChartProps } from "../../../visualizations/time-series-chart/time-series-chart.interfaces";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2.styles";
import {
    MessageDisplayState,
    PreviewChartProps,
} from "./preview-chart.interfaces";
import { usePreviewChartStyles } from "./preview-chart.styles";

export const PreviewChart: FunctionComponent<PreviewChartProps> = ({
    alert,
    displayState,
    subtitle,
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
    const [timeSeriesOptions, setTimeSeriesOptions] =
        useState<TimeSeriesChartProps>();
    const [detectionEvaluations, setDetectionEvaluations] =
        useState<DetectionEvaluation[]>();
    const [selectedEvaluationToDisplay, setSelectedEvaluationToDisplay] =
        useState<string>("");
    const [alertForCurrentEvaluation, setAlertForCurrentEvaluation] =
        useState<EditableAlert>();

    const {
        getEvaluation,
        errorMessages: getEvaluationRequestErrors,
        status: getEvaluationStatus,
    } = useGetEvaluation();

    const { getAlertInsight, status: getAlertInsightStatus } =
        useGetAlertInsight();

    const fetchAlertEvaluation = async (
        start: number,
        end: number
    ): Promise<void> => {
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

        if (evaluations.length === 1) {
            setSelectedEvaluationToDisplay(
                generateNameForDetectionResult(evaluations[0])
            );
        } else if (evaluations.length > 1) {
            // Reset what's chosen if the current selected is not in the data
            if (
                evaluations.find(
                    (evaluation) =>
                        selectedEvaluationToDisplay ===
                        generateNameForDetectionResult(evaluation)
                ) === undefined
            ) {
                setSelectedEvaluationToDisplay(
                    generateNameForDetectionResult(evaluations[0])
                );
            }
        }

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

    useEffect(() => {
        if (detectionEvaluations) {
            const detectionEvaluation = detectionEvaluations.find(
                (evaluation) =>
                    generateNameForDetectionResult(evaluation) ===
                    selectedEvaluationToDisplay
            );

            if (!detectionEvaluation) {
                return;
            }

            const timeseriesConfiguration = generateChartOptionsForAlert(
                detectionEvaluation,
                detectionEvaluation.anomalies,
                t
            );

            timeseriesConfiguration.brush = false;
            timeseriesConfiguration.zoom = true;

            setTimeSeriesOptions(timeseriesConfiguration);
        }
    }, [detectionEvaluations, selectedEvaluationToDisplay]);

    useEffect(() => {
        // If alert changes, reset the evaluation data
        setDetectionEvaluations(undefined);
    }, [alert]);

    const handleAutoRangeClick = (): void => {
        getAlertInsight({ alert }).then(
            (insights) => {
                if (insights) {
                    searchParams.set(
                        TimeRangeQueryStringKey.START_TIME,
                        insights.defaultStartTime.toString()
                    );
                    searchParams.set(
                        TimeRangeQueryStringKey.END_TIME,
                        insights.defaultEndTime.toString()
                    );
                    setSearchParams(searchParams, { replace: true });
                    fetchAlertEvaluation(
                        insights.defaultStartTime,
                        insights.defaultEndTime
                    );
                } else {
                    fetchAlertEvaluation(startTime, endTime);
                }
            },
            () => {
                // If API fails use current start and end
                fetchAlertEvaluation(startTime, endTime);
            }
        );
    };

    return (
        <LoadingErrorStateSwitch
            isError={false}
            isLoading={
                getEvaluationStatus === ActionStatus.Working ||
                getAlertInsightStatus === ActionStatus.Working
            }
            loadingState={
                <Box position="relative">
                    <SkeletonV1
                        animation="pulse"
                        delayInMS={0}
                        height={300}
                        variant="rect"
                    />
                </Box>
            }
        >
            {/** Header Section **/}
            <Grid
                container
                item
                alignItems="center"
                justifyContent="space-between"
                xs={12}
            >
                <Grid item>
                    <Typography variant="h6">
                        {t("label.alert-preview")}
                    </Typography>
                    <Typography variant="body2">{subtitle}</Typography>
                </Grid>
                {!isEqual(alertForCurrentEvaluation, alert) &&
                    timeSeriesOptions && (
                        <Grid item>
                            <Alert severity="warning" variant="outlined">
                                {t("message.chart-data-not-reflective")}
                            </Alert>
                        </Grid>
                    )}
                <Grid item>
                    <Button
                        color="primary"
                        disabled={
                            displayState !== MessageDisplayState.GOOD_TO_PREVIEW
                        }
                        variant="outlined"
                        onClick={() => {
                            if (timeSeriesOptions) {
                                fetchAlertEvaluation(startTime, endTime);
                            } else {
                                handleAutoRangeClick();
                            }
                        }}
                    >
                        <RefreshIcon fontSize="small" />
                        {t("label.reload-preview")}
                    </Button>
                </Grid>
            </Grid>

            <Grid item xs={12}>
                {/** When user has not selected alert template yet **/}
                {displayState === MessageDisplayState.SELECT_TEMPLATE && (
                    <Box marginTop={1} position="relative">
                        <Box className={previewChartClasses.alertContainer}>
                            <Grid container justifyContent="space-around">
                                <Grid item>
                                    <Alert
                                        className={
                                            sharedWizardClasses.infoAlert
                                        }
                                        severity="info"
                                    >
                                        {t(
                                            "message.select-a-template-to-preview"
                                        )}
                                    </Alert>
                                </Grid>
                            </Grid>
                        </Box>
                        <Box width="100%">
                            <ChartSkeleton />
                        </Box>
                    </Box>
                )}

                {/** When user has needs to fill out the required fields **/}
                {displayState ===
                    MessageDisplayState.FILL_TEMPLATE_PROPERTY_VALUES && (
                    <Box marginTop={1} position="relative">
                        <Box className={previewChartClasses.alertContainer}>
                            <Grid container justifyContent="space-around">
                                <Grid item>
                                    <Alert
                                        className={
                                            sharedWizardClasses.warningAlert
                                        }
                                        severity="warning"
                                    >
                                        {t(
                                            "message.complete-missing-information-to-see-preview"
                                        )}
                                    </Alert>
                                </Grid>
                            </Grid>
                        </Box>
                        <Box width="100%">
                            <ChartSkeleton />
                        </Box>
                    </Box>
                )}

                {/** Prompt use to preview the chart **/}
                {displayState === MessageDisplayState.GOOD_TO_PREVIEW && (
                    <Box marginTop={1} minHeight={100} position="relative">
                        {/** When there is no data at all **/}
                        {!detectionEvaluations && !timeSeriesOptions && (
                            <Box marginTop={1} position="relative">
                                <Box
                                    className={
                                        previewChartClasses.alertContainer
                                    }
                                >
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
                                                variant="text"
                                                onClick={handleAutoRangeClick}
                                            >
                                                <RefreshIcon fontSize="large" />
                                            </Button>
                                        </Grid>
                                    </Grid>
                                </Box>
                                <Box width="100%">
                                    <ChartSkeleton />
                                </Box>
                            </Box>
                        )}

                        {timeSeriesOptions && (
                            <Grid
                                container
                                item
                                justifyContent="space-between"
                                xs={12}
                            >
                                <Grid item>
                                    <Grid
                                        container
                                        alignItems="center"
                                        justifyContent="flex-start"
                                    >
                                        {!!detectionEvaluations &&
                                            detectionEvaluations.length > 1 && (
                                                <>
                                                    <Grid item>
                                                        <span>
                                                            {t(
                                                                "label.dimension-expression"
                                                            )}
                                                            :
                                                        </span>
                                                    </Grid>
                                                    <Grid item>
                                                        <FormControl>
                                                            <Select
                                                                disableUnderline
                                                                inputProps={{
                                                                    className:
                                                                        previewChartClasses.selected,
                                                                }}
                                                                value={
                                                                    selectedEvaluationToDisplay
                                                                }
                                                                onChange={(
                                                                    event
                                                                ) =>
                                                                    setSelectedEvaluationToDisplay(
                                                                        event
                                                                            .target
                                                                            .value as unknown as string
                                                                    )
                                                                }
                                                            >
                                                                {detectionEvaluations.map(
                                                                    (
                                                                        detectionEvaluation
                                                                    ) => {
                                                                        const name =
                                                                            generateNameForDetectionResult(
                                                                                detectionEvaluation
                                                                            );

                                                                        return (
                                                                            <MenuItem
                                                                                key={
                                                                                    name
                                                                                }
                                                                                value={
                                                                                    name
                                                                                }
                                                                            >
                                                                                {
                                                                                    name
                                                                                }
                                                                            </MenuItem>
                                                                        );
                                                                    }
                                                                )}
                                                            </Select>
                                                        </FormControl>
                                                    </Grid>
                                                </>
                                            )}
                                    </Grid>
                                </Grid>
                                <Grid item>
                                    <TimeRangeButtonWithContext
                                        onTimeRangeChange={(start, end) =>
                                            displayState ===
                                                MessageDisplayState.GOOD_TO_PREVIEW &&
                                            fetchAlertEvaluation(start, end)
                                        }
                                    />
                                </Grid>
                            </Grid>
                        )}

                        {timeSeriesOptions && (
                            <Box marginTop={1}>
                                <TimeSeriesChart
                                    height={300}
                                    {...timeSeriesOptions}
                                />
                            </Box>
                        )}
                    </Box>
                )}
            </Grid>
        </LoadingErrorStateSwitch>
    );
};
