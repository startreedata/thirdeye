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
import { Button, ButtonGroup, Popover } from "@material-ui/core";
import CalendarTodayIcon from "@material-ui/icons/CalendarToday";
import KeyboardArrowLeft from "@material-ui/icons/KeyboardArrowLeft";
import KeyboardArrowRight from "@material-ui/icons/KeyboardArrowRight";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { TooltipV1 } from "../../../platform/components";
import { formatTimeRangeDuration } from "../../../utils/time-range/time-range.util";
import { WEEK_IN_MILLISECONDS } from "../../../utils/time/time.util";
import { TimeRangeSelectorPopoverContent } from "../time-range-selector-popover-content/time-range-selector-popover-content.component";
import { TimeRangeButtonProps } from "./time-range-button.interfaces";

enum Direction {
    BACK,
    FORWARD,
}

export const TimeRangeButton: FunctionComponent<TimeRangeButtonProps> = ({
    timeRangeDuration,
    recentCustomTimeRangeDurations,
    onChange,
    maxDate,
    minDate,
    btnGroupColor = "secondary",
    hideQuickExtend,
    timezone,
}: TimeRangeButtonProps) => {
    const { t } = useTranslation();
    const [timeRangeSelectorAnchorElement, setTimeRangeSelectorAnchorElement] =
        useState<HTMLElement | null>();

    const handleTimeRangeSelectorClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setTimeRangeSelectorAnchorElement(event.currentTarget);
    };

    const handleTimeRangeSelectorClose = (): void => {
        setTimeRangeSelectorAnchorElement(null);
    };

    const handleWeekExtensionClick = (direction: Direction): void => {
        const copiedTimeRange = { ...timeRangeDuration };
        if (direction === Direction.BACK) {
            copiedTimeRange.startTime =
                copiedTimeRange.startTime - WEEK_IN_MILLISECONDS;
        } else {
            copiedTimeRange.endTime = Math.min(
                copiedTimeRange.endTime + WEEK_IN_MILLISECONDS,
                Date.now()
            );
        }
        onChange && onChange(copiedTimeRange);
    };

    const isDateEndExtendable = maxDate
        ? timeRangeDuration.endTime + WEEK_IN_MILLISECONDS <= maxDate
        : timeRangeDuration.endTime + WEEK_IN_MILLISECONDS < Date.now();
    const isDateStartExtendable = minDate
        ? timeRangeDuration.startTime - WEEK_IN_MILLISECONDS >= minDate
        : true;

    return (
        <>
            <ButtonGroup color={btnGroupColor} variant="outlined">
                {!hideQuickExtend && (
                    <Button
                        color={btnGroupColor}
                        disabled={!isDateStartExtendable}
                        variant="outlined"
                        onClick={() => handleWeekExtensionClick(Direction.BACK)}
                    >
                        <TooltipV1
                            placement="top"
                            title={t("message.extend-entity-by-week", {
                                entity: `${t("label.time-range")} ${t(
                                    "label.start"
                                )}`,
                            })}
                        >
                            <KeyboardArrowLeft
                                style={{ fontSize: "0.9375rem" }}
                            />
                        </TooltipV1>
                    </Button>
                )}

                <Button
                    color={btnGroupColor}
                    startIcon={<CalendarTodayIcon />}
                    variant="outlined"
                    onClick={handleTimeRangeSelectorClick}
                >
                    {formatTimeRangeDuration(timeRangeDuration, timezone)}
                </Button>
                {!hideQuickExtend && (
                    <Button
                        color={btnGroupColor}
                        disabled={!isDateEndExtendable}
                        variant="outlined"
                        onClick={() =>
                            handleWeekExtensionClick(Direction.FORWARD)
                        }
                    >
                        <TooltipV1
                            placement="top"
                            title={t("message.extend-entity-by-week", {
                                entity: `${t("label.time-range")} ${t(
                                    "label.end"
                                )}`,
                            })}
                        >
                            <KeyboardArrowRight
                                style={{ fontSize: "0.9375rem" }}
                            />
                        </TooltipV1>
                    </Button>
                )}
            </ButtonGroup>
            {/* Time range selector */}
            <Popover
                anchorEl={timeRangeSelectorAnchorElement}
                open={Boolean(timeRangeSelectorAnchorElement)}
                onClose={handleTimeRangeSelectorClose}
            >
                <TimeRangeSelectorPopoverContent
                    maxDate={maxDate}
                    minDate={minDate}
                    recentCustomTimeRangeDurations={
                        recentCustomTimeRangeDurations
                    }
                    timeRangeDuration={timeRangeDuration}
                    timezone={timezone}
                    onChange={onChange}
                    onClose={handleTimeRangeSelectorClose}
                />
            </Popover>
        </>
    );
};
