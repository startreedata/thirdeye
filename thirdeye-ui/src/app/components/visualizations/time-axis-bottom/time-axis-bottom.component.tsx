import { withWidth } from "@material-ui/core";
import { AxisBottom, Text, TickRendererProps } from "@visx/visx";
import React, { FunctionComponent, ReactNode } from "react";
import {
    formatDateTimeForAxis,
    getTimeTickValuesForAxis,
    SEPARATOR_DATE_TIME,
} from "../../../utils/visualization/visualization.util";
import { TimeAxisBottomProps } from "./time-axis-bottom.interfaces";
import { useTimeAxisBottomStyles } from "./time-axis-bottom.styles";

const NUM_TICKS_XS = 4;
const NUM_TICKS_SM = 5;
const NUM_TICKS_MD = 8;
const NUM_TICKS_LG = 9;

// Customization of visx time axis with formatted tick labels based on scale domain interval
const TimeAxisBottomInternal: FunctionComponent<TimeAxisBottomProps> = (
    props: TimeAxisBottomProps
) => {
    const timeAxisBottomClasses = useTimeAxisBottomStyles();

    const tickFormatter = (date: number | { valueOf(): number }): string => {
        return formatDateTimeForAxis(date, props.scale);
    };

    const getTickValues = (): number[] => {
        // Determine number of ticks to display based on input or screen width
        let numTicks = props.numTicks;
        if (!numTicks && props.width === "xs") {
            numTicks = NUM_TICKS_XS;
        } else if (!numTicks && props.width === "sm") {
            numTicks = NUM_TICKS_SM;
        } else if (!numTicks && props.width === "md") {
            numTicks = NUM_TICKS_MD;
        } else if (!numTicks && props.width === "lg") {
            numTicks = NUM_TICKS_LG;
        }

        return getTimeTickValuesForAxis(numTicks as number, props.scale);
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

export const TimeAxisBottom = withWidth()(TimeAxisBottomInternal);
