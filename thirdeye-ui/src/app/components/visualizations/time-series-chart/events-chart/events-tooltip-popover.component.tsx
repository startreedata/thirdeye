import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTooltipStyles } from "../tooltip/tooltip.styles";
import { EventsTooltipPopoverProps } from "./events-chart.interfaces";

export const EventsTooltipPopover: FunctionComponent<
    EventsTooltipPopoverProps
> = ({ events, colorScale }) => {
    const timeSeriesChartTooltipClasses = useTooltipStyles();

    return (
        <Grid container>
            <Grid item xs={12}>
                <table className={timeSeriesChartTooltipClasses.table}>
                    <tbody>
                        {events.map((event) => {
                            return (
                                <tr key={event.id}>
                                    <td style={{ color: colorScale(event.id) }}>
                                        {event.name}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </Grid>
        </Grid>
    );
};
