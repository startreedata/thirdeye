/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid, Typography } from "@material-ui/core";
import { map } from "lodash";
import React, { FunctionComponent } from "react";
import { EventCardProps } from "../event-card/event-card.interfaces";

export const EventCardV1: FunctionComponent<EventCardProps> = ({
    event,
}: EventCardProps) => {
    return event?.targetDimensionMap ? (
        <Grid container spacing={2}>
            {map(event.targetDimensionMap, (value: string[], key: string) => (
                <Grid item md={3} xs={12}>
                    <Typography color="textSecondary" variant="subtitle2">
                        {key}
                    </Typography>
                    <Typography variant="body2">{value.join(", ")}</Typography>
                </Grid>
            ))}
        </Grid>
    ) : null;
};
