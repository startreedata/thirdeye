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

export const TICK_FORMAT_SEPARATOR_DATE_TIME = "@";

// Customization of visx time axis with formatted tick labels based on time range
export const VisxCustomTimeAxisBottom: FunctionComponent<VisxCustomTimeAxisBottomProps> = (
    props: VisxCustomTimeAxisBottomProps
) => {
    const tickFormatter = (
        date: Date | number | { valueOf(): number }
    ): string => {
        if (!date || isEmpty(props.scale.domain())) {
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
        const startTime = props.scale.domain()[0];
        const endTimeTime = props.scale.domain()[1];

        return formatDateTime(dateToFormat, startTime, endTimeTime);
    };

    // Returns formatted string representation of date based on time interval between start and
    // end time
    // For example:
    // Time interval > 2 years - YYYY
    // Time interval > 2 months - MMM YYYY
    // Time interval > 2 days - MMM DD, YYYY
    // Time interval > 2 hours - MMM DD, YY TICK_FORMAT_SEPARATOR_DATE_TIME HH:MM AM/PM
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
            // MMM YYYY
            return formatMonthOfYear(date.getTime());
        }

        if (duration.as("days") > 2) {
            // MMM DD, YYYY
            return formatDate(date.getTime());
        }

        // MMM DD, YY TICK_FORMAT_SEPARATOR_DATE_TIME HH:MM AM/PM
        return (
            formatDate(date.getTime()) +
            TICK_FORMAT_SEPARATOR_DATE_TIME +
            formatTime(date.getTime())
        );
    };

    // Renders date from tick renderer props based on whether or not it contains
    // TICK_FORMAT_SEPARATOR_DATE_TIME
    const tickComponentRenderer = (
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
            tickRendererProps.formattedValue.includes(
                TICK_FORMAT_SEPARATOR_DATE_TIME
            )
        ) {
            // String contains both date and time which need to be rendered in two parts one below
            // the other
            const dateParts = tickRendererProps.formattedValue.split(
                TICK_FORMAT_SEPARATOR_DATE_TIME
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
            left={props.left}
            numTicks={props.numTicks}
            scale={props.scale}
            tickClassName={props.tickClassName}
            tickComponent={tickComponentRenderer}
            tickFormat={tickFormatter}
            top={props.top}
        />
    );
};
