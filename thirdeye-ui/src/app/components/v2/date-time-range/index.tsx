/*
 * Copyright 2024 StarTree Inc
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
import React, { useState } from "react";
import { DateTimeUi } from "../date-time";
import { useDateTimeRangeStyles } from "./styles";
import { Button } from "@material-ui/core";
import {
    QuckTimeRangeSelect,
    TimeRange,
    TimeRangeDuration,
} from "./quick-select";
import { formatDateAndTimeV1 } from "../../../platform/utils";

type DateTimeRangeProps = {
    from: { label: string; date: number };
    to: { label: string; date: number };
    onDateApply: ({
        timeRange,
        endTime,
        startTime,
    }: {
        timeRange: TimeRange;
        endTime: number;
        startTime: number;
    }) => void;
    onCancel?: () => void;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    selectedRange?: TimeRange;
};

export const DateTimeRange = ({
    from,
    to,
    onDateApply,
    onCancel,
    recentCustomTimeRangeDurations,
    selectedRange,
}: DateTimeRangeProps): JSX.Element => {
    const componentStyles = useDateTimeRangeStyles();
    const [fromDate, setFromDate] = useState(from.date);
    const [toDate, setToDate] = useState(to.date);
    const [selectedTimeRange, setSelectedTimeRange] = useState(
        selectedRange || TimeRange.CUSTOM
    );

    const onFromDateSelect = (date: any): void => {
        setSelectedTimeRange(TimeRange.CUSTOM);
        setFromDate(date.ts);
    };

    const onToDateSelect = (date: any): void => {
        setSelectedTimeRange(TimeRange.CUSTOM);
        setToDate(date.ts);
    };

    const onApply = (): void => {
        onDateApply({
            timeRange: selectedTimeRange,
            startTime: fromDate,
            endTime: toDate,
        });
    };

    const onQuickSelect = (range: TimeRangeDuration): void => {
        setSelectedTimeRange(range.timeRange);
        setFromDate(range.startTime);
        setToDate(range.endTime);
    };

    return (
        <div className={componentStyles.container}>
            <QuckTimeRangeSelect
                recentCustomTimeRangeDurations={recentCustomTimeRangeDurations}
                selectedTimeRange={selectedTimeRange}
                onChange={onQuickSelect}
            />
            <div className={componentStyles.separator} />
            <div className={componentStyles.dateTimecontainer}>
                <div className={componentStyles.dateTimePicker}>
                    <div>
                        <div className={componentStyles.calendarHeader}>
                            <span className="label">From:</span>{" "}
                            <span className="date">
                                {formatDateAndTimeV1(fromDate)}
                            </span>
                        </div>
                        <DateTimeUi
                            date={fromDate}
                            label={from.label}
                            onDateTimeChange={onFromDateSelect}
                        />
                    </div>
                    <div>
                        <div className={componentStyles.calendarHeader}>
                            <span className="label">To:</span>{" "}
                            <span className="date">
                                {formatDateAndTimeV1(toDate)}
                            </span>
                        </div>
                        <DateTimeUi
                            disableFuture
                            date={toDate}
                            label={to.label}
                            minDate={from.date}
                            onDateTimeChange={onToDateSelect}
                        />
                    </div>
                </div>
                <div className={componentStyles.actionButtons}>
                    <Button variant="outlined" onClick={onCancel}>
                        Cancel
                    </Button>
                    <Button variant="contained" onClick={onApply}>
                        Apply
                    </Button>
                </div>
            </div>
        </div>
    );
};
