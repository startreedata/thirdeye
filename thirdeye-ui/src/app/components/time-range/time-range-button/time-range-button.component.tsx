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

export const TimeRangeButton: FunctionComponent<TimeRangeButtonProps> = ({
    timeRangeDuration,
    recentCustomTimeRangeDurations,
    onChange,
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

    const handleWeekExtensionClick = (direction: number): void => {
        const copiedTimeRange = { ...timeRangeDuration };
        if (direction > 0) {
            copiedTimeRange.endTime =
                copiedTimeRange.endTime + WEEK_IN_MILLISECONDS;
        } else {
            copiedTimeRange.startTime =
                copiedTimeRange.startTime - WEEK_IN_MILLISECONDS;
        }
        onChange && onChange(copiedTimeRange);
    };

    return (
        <>
            <ButtonGroup color="secondary" variant="outlined">
                <Button
                    color="secondary"
                    variant="outlined"
                    onClick={() => handleWeekExtensionClick(-1)}
                >
                    <TooltipV1
                        placement="top"
                        title={t("message.extend-entity-by-week", {
                            entity: `${t("label.time-range")} ${t(
                                "label.start"
                            )}`,
                        })}
                    >
                        <KeyboardArrowLeft style={{ fontSize: "0.9375rem" }} />
                    </TooltipV1>
                </Button>
                <Button
                    color="secondary"
                    startIcon={<CalendarTodayIcon />}
                    variant="outlined"
                    onClick={handleTimeRangeSelectorClick}
                >
                    {formatTimeRangeDuration(timeRangeDuration)}
                </Button>
                <Button
                    color="secondary"
                    variant="outlined"
                    onClick={() => handleWeekExtensionClick(1)}
                >
                    <TooltipV1
                        placement="top"
                        title={t("message.extend-entity-by-week", {
                            entity: `${t("label.time-range")} ${t(
                                "label.end"
                            )}`,
                        })}
                    >
                        <KeyboardArrowRight style={{ fontSize: "0.9375rem" }} />
                    </TooltipV1>
                </Button>
            </ButtonGroup>
            {/* Time range selector */}
            <Popover
                anchorEl={timeRangeSelectorAnchorElement}
                open={Boolean(timeRangeSelectorAnchorElement)}
                onClose={handleTimeRangeSelectorClose}
            >
                <TimeRangeSelectorPopoverContent
                    recentCustomTimeRangeDurations={
                        recentCustomTimeRangeDurations
                    }
                    timeRangeDuration={timeRangeDuration}
                    onChange={onChange}
                    onClose={handleTimeRangeSelectorClose}
                />
            </Popover>
        </>
    );
};
