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

import { Box, Typography } from "@material-ui/core";
import { BorderHorizontalOutlined, ZoomIn } from "@material-ui/icons";
import { ToggleButton, ToggleButtonGroup } from "@material-ui/lab";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { createTimeRangeDuration } from "../../../utils/time-range/time-range.util";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { TimeRangeButton } from "../../time-range/time-range-button/time-range-button.component";
import { TimeRange } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { CreateAnomaliesDateRangePickerProps } from "./create-anomalies-date-range-picker.interfaces";
import { DragOptions } from "./create-anomalies-date-range-picker.utils";

export const CreateAnomaliesDateRangePicker: FunctionComponent<CreateAnomaliesDateRangePickerProps> =
    ({
        timezone,
        setValidAnomalyDateRange,
        captureDateRangeFromChart,
        setCaptureDateRangeFromChart,
        dateRange,
    }) => {
        const { t } = useTranslation();

        const timeRangeDuration = createTimeRangeDuration(
            TimeRange.CUSTOM,
            dateRange[0],
            dateRange[1]
        );

        const dragState: keyof typeof DragOptions = captureDateRangeFromChart
            ? DragOptions.SELECT
            : DragOptions.ZOOM;

        return (
            <>
                <InputSection
                    fullWidth
                    inputComponent={
                        <Box display="flex" flexDirection="column" gridGap={4}>
                            <TimeRangeButton
                                hideQuickExtend
                                timeRangeDuration={timeRangeDuration}
                                timezone={timezone}
                                onChange={({ startTime, endTime }) => {
                                    setValidAnomalyDateRange([
                                        startTime,
                                        endTime,
                                    ]);
                                }}
                            />
                            <Typography color="textSecondary" variant="caption">
                                {t(
                                    "message.select-the-start-and-end-date-time-range-for-the-anomalous-behavior"
                                )}
                            </Typography>
                        </Box>
                    }
                    label={t("label.date-range")}
                />
                <br />
                <InputSection
                    fullWidth
                    inputComponent={
                        <Box display="flex" flexDirection="column" gridGap={4}>
                            <ToggleButtonGroup
                                exclusive
                                value={dragState}
                                onChange={(_, newValue) => {
                                    setCaptureDateRangeFromChart(
                                        newValue === DragOptions.SELECT
                                    );
                                }}
                            >
                                <ToggleButton
                                    size="small"
                                    value={DragOptions.SELECT}
                                >
                                    <BorderHorizontalOutlined
                                        color={
                                            dragState === DragOptions.SELECT
                                                ? "primary"
                                                : "secondary"
                                        }
                                    />
                                    <Typography
                                        color={
                                            dragState === DragOptions.SELECT
                                                ? "primary"
                                                : "secondary"
                                        }
                                        variant="button"
                                    >
                                        {t("label.select")}
                                    </Typography>
                                </ToggleButton>
                                <ToggleButton
                                    size="small"
                                    value={DragOptions.ZOOM}
                                >
                                    <ZoomIn
                                        color={
                                            dragState === DragOptions.ZOOM
                                                ? "primary"
                                                : "secondary"
                                        }
                                    />
                                    <Typography
                                        color={
                                            dragState === DragOptions.ZOOM
                                                ? "primary"
                                                : "secondary"
                                        }
                                        variant="button"
                                    >
                                        {t("label.zoom")}
                                    </Typography>
                                </ToggleButton>
                            </ToggleButtonGroup>
                            <Typography color="textSecondary" variant="caption">
                                {captureDateRangeFromChart
                                    ? t(
                                          "message.drag-on-chart-to-select-anomaly-date-range"
                                      )
                                    : t("message.drag-on-chart-to-zoom")}
                            </Typography>
                        </Box>
                    }
                    label={t("message.mouse-drag-behavior")}
                />
            </>
        );
    };
