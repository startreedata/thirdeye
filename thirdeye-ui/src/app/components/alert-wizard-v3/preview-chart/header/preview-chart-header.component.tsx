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
import { Button, Grid } from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import { Alert } from "@material-ui/lab";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { determineTimezoneFromAlertInEvaluation } from "../../../../utils/alerts/alerts.util";
import { TimeRangeButtonWithContext } from "../../../time-range/time-range-button-with-context/time-range-button.component";
import { PreviewChartHeaderProps } from "./preview-chart-header.interfaces";

export const PreviewChartHeader: FunctionComponent<PreviewChartHeaderProps> = ({
    alertInsight,
    getEvaluationStatus,
    disableReload,
    onReloadClick,
    onStartEndChange,
    showConfigurationNotReflective,
    showTimeRange = true,
}) => {
    const { t } = useTranslation();

    return (
        <>
            {getEvaluationStatus !== ActionStatus.Initial && (
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item>
                        <Button
                            color="primary"
                            disabled={disableReload}
                            variant="outlined"
                            onClick={onReloadClick}
                        >
                            <RefreshIcon fontSize="small" />
                            {t("label.reload-preview")}
                        </Button>
                    </Grid>
                    {showConfigurationNotReflective &&
                        getEvaluationStatus !== ActionStatus.Working && (
                            <Grid item>
                                <Alert severity="warning" variant="outlined">
                                    {t("message.chart-data-not-reflective")}
                                </Alert>
                            </Grid>
                        )}
                    <Grid item>
                        {showTimeRange && (
                            <TimeRangeButtonWithContext
                                hideQuickExtend
                                btnGroupColor="primary"
                                maxDate={alertInsight?.datasetEndTime}
                                minDate={alertInsight?.datasetStartTime}
                                timezone={determineTimezoneFromAlertInEvaluation(
                                    alertInsight?.templateWithProperties
                                )}
                                onTimeRangeChange={onStartEndChange}
                            />
                        )}
                    </Grid>
                </Grid>
            )}
        </>
    );
};
