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
import { Box } from "@material-ui/core";
import React, { FunctionComponent, ReactNode } from "react";
import { useTranslation } from "react-i18next";

// import { PreviewChartProps } from "./preview-chart.interfaces";
import { usePreviewChartStyles } from "./styles";
import { PreviewChartHeader } from "./chart-header";
import { ChartContentV2 } from "../alert-wizard-v3/preview-chart/chart-content-v2/chart-content-v2.component";
import {
    AlertEvaluation,
    EditableAlert,
} from "../../rest/dto/alert.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { LoadingErrorStateSwitch } from "../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { SkeletonV1 } from "../../platform/components";

enum LegendPlacement {
    TOP = "top",
    BOTTOM = "bottom",
}

type PreviewChartProps = {
    alert: EditableAlert;
    onChartDataLoadSuccess?: () => void;
    hideCallToActionPrompt?: boolean;
    disableReload?: boolean;
    alertEvaluation: AlertEvaluation | null;
    onReload?: () => void;
    onAlertPropertyChange?: (
        contents: Partial<EditableAlert>,
        isTotalReplace?: boolean
    ) => void;
    showTimeRange?: boolean;
    legendsPlacement?: `${LegendPlacement}`;
    isEvaluationDataStale?: boolean;
    getEvaluationDataStatus: ActionStatus;
    onReloadData: () => void;
    handleDateRangeUpdate?: (start: number, end: number) => void;
    dateRange: { startTime: number; endTime: number; timezone: string };
    showOnlyActivity?: boolean;
    children?: ReactNode;
    additionalCTA: ReactNode;
};

export const PreviewChartMain: FunctionComponent<PreviewChartProps> = ({
    alert,
    hideCallToActionPrompt,
    disableReload,
    showTimeRange = true,
    children,
    legendsPlacement,
    alertEvaluation,
    isEvaluationDataStale,
    getEvaluationDataStatus,
    onReloadData,
    handleDateRangeUpdate,
    dateRange,
    onAlertPropertyChange,
    showOnlyActivity = false,
    additionalCTA,
}) => {
    const classes = usePreviewChartStyles();
    const { t } = useTranslation();

    return (
        <>
            {/** Header Section **/}
            <PreviewChartHeader
                dateRange={{
                    startTime: dateRange?.startTime,
                    endTime: dateRange?.endTime,
                    timezone: dateRange?.timezone,
                }}
                disableReload={disableReload}
                getEvaluationStatus={getEvaluationDataStatus}
                showConfigurationNotReflective={isEvaluationDataStale}
                showTimeRange={showTimeRange}
                onReloadClick={onReloadData}
                onStartEndChange={(newStart, newEnd) => {
                    handleDateRangeUpdate &&
                        handleDateRangeUpdate(newStart, newEnd);
                }}
            >
                {children}
            </PreviewChartHeader>
            <Box className={classes.chartContainer} marginTop={2}>
                <LoadingErrorStateSwitch
                    errorState={
                        <Box pb={20} pt={20}>
                            <NoDataIndicator>
                                {t(
                                    "message.experienced-issues-loading-chart-data-try"
                                )}
                            </NoDataIndicator>
                        </Box>
                    }
                    isError={getEvaluationDataStatus === ActionStatus.Error}
                    isLoading={getEvaluationDataStatus === ActionStatus.Working}
                    loadingState={
                        <Box paddingTop={1}>
                            <SkeletonV1
                                animation="pulse"
                                delayInMS={0}
                                height={300}
                                variant="rect"
                            />
                        </Box>
                    }
                >
                    <ChartContentV2
                        isSearchEnabled
                        showLoadButton
                        additionalCTA={additionalCTA}
                        alert={alert}
                        alertEvaluation={alertEvaluation}
                        evaluationTimeRange={{
                            startTime: dateRange?.startTime,
                            endTime: dateRange?.endTime,
                        }}
                        hideCallToActionPrompt={hideCallToActionPrompt}
                        legendsPlacement={legendsPlacement}
                        showDeleteIcon={false}
                        showOnlyActivity={showOnlyActivity}
                        onAlertPropertyChange={onAlertPropertyChange}
                        onReloadClick={onReloadData}
                    />
                </LoadingErrorStateSwitch>
            </Box>
        </>
    );
};
