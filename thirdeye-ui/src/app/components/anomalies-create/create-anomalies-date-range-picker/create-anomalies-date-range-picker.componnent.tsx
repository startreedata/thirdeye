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

import { Box, Typography } from "@material-ui/core";
import { BorderHorizontalOutlined, ZoomIn } from "@material-ui/icons";
import { ToggleButton, ToggleButtonGroup } from "@material-ui/lab";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { createTimeRangeDuration } from "../../../utils/time-range/time-range.util";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { TimeRangeButton } from "../../time-range/time-range-button/time-range-button.component";
import { TimeRangeButtonProps } from "../../time-range/time-range-button/time-range-button.interfaces";
import { TimeRange } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { CreateAnomaliesDateRangePickerProps } from "./create-anomalies-date-range-picker.interfaces";
import { DragOptions } from "./create-anomalies-date-range-picker.utils";

export const CreateAnomaliesDateRangePicker: FunctionComponent<CreateAnomaliesDateRangePickerProps> =
    ({
        timezone,
        findClosestAppropriateTimestamp,
        handleSetField,
        captureDateRangeFromChart,
        setCaptureDateRangeFromChart,
        formFields,
    }) => {
        const { t } = useTranslation();

        const timeRangeDuration = createTimeRangeDuration(
            TimeRange.CUSTOM,
            formFields.dateRange[0],
            formFields.dateRange[1]
        );

        const handleUpdateAnomalyTimeRange: TimeRangeButtonProps["onChange"] =
            ({ startTime, endTime }) => {
                const start =
                    findClosestAppropriateTimestamp(startTime) || startTime;

                const end =
                    findClosestAppropriateTimestamp(Math.max(start, endTime)) ||
                    Math.max(start, endTime); // Ensure that the user does not set the end time before the start time

                handleSetField("dateRange", [start, end]);
            };

        const dragState: keyof typeof DragOptions = captureDateRangeFromChart
            ? DragOptions.SELECT
            : DragOptions.ZOOM;

        return (
            <>
                <InputSection
                    fullWidth
                    inputComponent={
                        <Box display="flex">
                            <TimeRangeButton
                                hideQuickExtend
                                timeRangeDuration={timeRangeDuration}
                                timezone={timezone}
                                onChange={handleUpdateAnomalyTimeRange}
                            />
                        </Box>
                    }
                    labelComponent={
                        <>
                            <Typography variant="body2">
                                {t("label.date-range")}
                            </Typography>
                            <Typography color="textSecondary" variant="caption">
                                {t(
                                    "message.select-the-start-and-end-date-time-range-for-the-anomalous-behavior"
                                )}
                            </Typography>
                        </>
                    }
                />
                <br />
                <InputSection
                    fullWidth
                    inputComponent={
                        <Box
                            alignItems="center"
                            display="flex"
                            gridGap={2}
                            ml={2}
                        >
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
                        </Box>
                    }
                    labelComponent={
                        <>
                            <Typography variant="body2">
                                {t("message.mouse-drag-behavior-on-chart")}
                            </Typography>
                            <Typography color="textSecondary" variant="caption">
                                {captureDateRangeFromChart
                                    ? t(
                                          "message.drag-on-chart-to-select-anomaly-date-range"
                                      )
                                    : t("message.drag-on-chart-to-zoom")}
                            </Typography>
                        </>
                    }
                />
            </>
        );
    };
