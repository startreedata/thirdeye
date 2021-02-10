import { AxisBottom, Text, TickRendererProps } from "@visx/visx";
import React, { FunctionComponent, ReactNode } from "react";
import {
    formatDateTimeForAxis,
    getTimeTickValuesForAxis,
    SEPARATOR_DATE_TIME,
} from "../../../utils/visualization/visualization.util";
import { TimeAxisBottomProps } from "./time-axis-bottom.interfaces";
import { useTimeAxisBottomStyles } from "./time-axis-bottom.styles";

// Customization of visx time axis with formatted tick labels based on scale domain interval
export const TimeAxisBottom: FunctionComponent<TimeAxisBottomProps> = (
    props: TimeAxisBottomProps
) => {
    const timeAxisBottomClasses = useTimeAxisBottomStyles();

    const tickFormatter = (date: number | { valueOf(): number }): string => {
        return formatDateTimeForAxis(date, props.scale);
    };

    const getTickValues = (): number[] => {
        return getTimeTickValuesForAxis(props.numTicks as number, props.scale);
    };

    // Renders formatted date from tick renderer props based on whether or not it contains
    // SEPARATOR_DATE_TIME
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
            tickRendererProps.formattedValue.includes(SEPARATOR_DATE_TIME)
        ) {
            // String contains both date and time which need to be rendered in two parts one below
            // the other
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
                    y={tickRendererProps.y + 2}
                >
                    {dateString}
                </Text>

                {timeString && (
                    <Text
                        textAnchor={tickRendererProps.textAnchor}
                        x={tickRendererProps.x}
                        y={tickRendererProps.y + 16}
                    >
                        {timeString}
                    </Text>
                )}
            </>
        );
    };

    return (
        <AxisBottom
            left={props.left}
            scale={props.scale}
            tickClassName={timeAxisBottomClasses.tick}
            tickComponent={tickComponentRenderer}
            tickFormat={tickFormatter}
            tickValues={getTickValues()}
            top={props.top}
        />
    );
};
