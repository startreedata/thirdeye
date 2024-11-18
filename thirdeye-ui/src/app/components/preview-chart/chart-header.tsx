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
import { previewChartHeaderStyles } from "./styles";
import { ActionStatus } from "../../rest/actions.interfaces";
import { TimeRangeButtonWithContext } from "../time-range/time-range-button-with-context-v2/time-range-button.component";

export interface PreviewChartHeaderProps {
    dateRange: { startTime?: number; endTime?: number; timezone?: string };
    getEvaluationStatus: ActionStatus;
    disableReload?: boolean;
    onReloadClick: () => void;
    onStartEndChange: (start: number, end: number) => void;
    showConfigurationNotReflective?: boolean;
    showTimeRange?: boolean;
}

export const PreviewChartHeader: FunctionComponent<PreviewChartHeaderProps> = ({
    dateRange,
    disableReload,
    onReloadClick,
    onStartEndChange,
    showConfigurationNotReflective,
    showTimeRange = true,
    children,
}) => {
    const { t } = useTranslation();
    const classes = previewChartHeaderStyles();

    return (
        <>
            <Grid container alignItems="center" justifyContent="space-between">
                {showTimeRange && (
                    <Grid item>
                        <TimeRangeButtonWithContext
                            hideQuickExtend
                            btnGroupColor="default"
                            maxDate={dateRange?.endTime}
                            minDate={dateRange?.startTime}
                            timezone={dateRange?.timezone}
                            onTimeRangeChange={onStartEndChange}
                        />
                    </Grid>
                )}
                <Grid item>
                    <Grid container>
                        <Grid item>
                            <Button
                                className={classes.refreshButton}
                                color="primary"
                                disabled={disableReload}
                                variant="outlined"
                                onClick={onReloadClick}
                            >
                                <RefreshIcon fontSize="small" />
                                {t("label.reload-preview")}
                            </Button>
                        </Grid>
                        <Grid item>{children}</Grid>
                    </Grid>
                </Grid>
                <Grid item>
                    {showConfigurationNotReflective && (
                        <Alert severity="warning" variant="outlined">
                            {t("message.chart-data-not-reflective")}
                        </Alert>
                    )}
                </Grid>
            </Grid>
        </>
    );
};
