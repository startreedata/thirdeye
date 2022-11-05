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
import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTooltipStyles } from "../tooltip/tooltip.styles";
import { EventsTooltipPopoverProps } from "./events-chart.interfaces";

export const EventsTooltipPopover: FunctionComponent<EventsTooltipPopoverProps> =
    ({ events, colorScale }) => {
        const timeSeriesChartTooltipClasses = useTooltipStyles();

        return (
            <Grid container>
                <Grid item xs={12}>
                    <table className={timeSeriesChartTooltipClasses.table}>
                        <tbody>
                            {events
                                .filter((event) => event.enabled)
                                .map((event) => {
                                    return (
                                        <tr key={event.id}>
                                            <td
                                                style={{
                                                    color: colorScale(event.id),
                                                }}
                                            >
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
