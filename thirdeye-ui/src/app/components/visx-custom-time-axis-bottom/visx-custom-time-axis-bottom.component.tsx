import { AxisBottom, Text, TickRendererProps } from "@visx/visx";
import { isEmpty } from "lodash";
import { Interval } from "luxon";
import React, { FunctionComponent, ReactNode } from "react";
import {
    formatDate,
    formatMonthOfYear,
    formatTime,
    formatYear,
} from "../../utils/date-time-util/date-time-util";
import { VisxCustomTimeAxisBottomProps } from "./visx-custom-time-axis-bottom.interfaces";

export const SEPARATOR_DATE_TIME = "@";

// This is a customization of visx time axis with formatted tick labels based on time range
export const VisxCustomTimeAxisBottom: FunctionComponent<VisxCustomTimeAxisBottomProps> = (
    props: VisxCustomTimeAxisBottomProps
) => {
    const tickFormatter = (
        date: Date | number | { valueOf(): number }
    ): string => {
        if (!date || !props.scale || isEmpty(props.scale.domain())) {
            return "";
        }

        let dateToFormat;
        if (date instanceof Date) {
            dateToFormat = date;
        }

        if (typeof date === "number") {
            dateToFormat = new Date(date);
        }

        dateToFormat = new Date(date.valueOf());
        const startTimeDate = props.scale.domain()[0];
        const endTimeTimeDate = props.scale.domain()[1];

        return formatDateTime(dateToFormat, startTimeDate, endTimeTimeDate);
    };

    // Returns formatted string representation of given date based on the time interval between
    // given start and end time
    // For example:
    // Time interval > 2 years - YYYY
    // Time interval > 2 months - MMM YYYY
    // Time interval > 2 days - MMM DD, YY
    // Time interval > 2 hours - MMM DD, YY SEPARATOR_DATE_TIME HH:MM AM/PM
    const formatDateTime = (
        date: Date,
        startTime: Date,
        endTime: Date
    ): string => {
        if (!date || !startTime || !endTime) {
            return "";
        }

        // Determine interval duration
        const duration = Interval.fromDateTimes(
            startTime,
            endTime
        ).toDuration();

        if (duration.as("years") > 2) {
            // YYYY
            return formatYear(date.getTime());
        }

        if (duration.as("months") > 2) {
            return formatMonthOfYear(date.getTime());
        }

        if (duration.as("days") > 2) {
            return formatDate(date.getTime());
        }

        return (
            formatDate(date.getTime()) +
            SEPARATOR_DATE_TIME +
            formatTime(date.getTime())
        );
    };

    // Renders date from tick renderer props based on whether or not it contains SEPARATOR_DATE_TIME
    const renderFormattedDate = (
        tickRendererProps: TickRendererProps
    ): ReactNode => {
        let dateString = "";
        let timeString = "";

        if (tickRendererProps && tickRendererProps.formattedValue) {
            // To begin with, assume the string can be rendered as is
            dateString = tickRendererProps.formattedValue;
        }

        if (
            tickRendererProps &&
            tickRendererProps.formattedValue &&
            tickRendererProps.formattedValue.includes(SEPARATOR_DATE_TIME)
        ) {
            // String contains both date and time which need to be rendered in two parts
            const dateParts = tickRendererProps.formattedValue.split(
                SEPARATOR_DATE_TIME
            );
            dateString = dateParts && dateParts[0] && dateParts[0];
            timeString = dateParts && dateParts[1] && dateParts[1];
        }

        return (
            <>
                <Text
                    textAnchor={tickRendererProps.textAnchor}
                    x={tickRendererProps.x}
                    y={tickRendererProps.y}
                >
                    {dateString}
                </Text>
                <Text
                    textAnchor={tickRendererProps.textAnchor}
                    x={tickRendererProps.x}
                    y={tickRendererProps.y + 10}
                >
                    {timeString}
                </Text>
            </>
        );
    };

    return (
        // Time axis
        <AxisBottom
            left={props.left || 0}
            numTicks={props.numTicks}
            scale={props.scale}
            tickClassName={props.tickClassName}
            tickComponent={renderFormattedDate}
            tickFormat={tickFormatter}
            top={props.top || 0}
        />
    );
};
