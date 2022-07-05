import { Grid, Typography } from "@material-ui/core";
import { map } from "lodash";
import React, { FC } from "react";
import { EventCardProps } from "../event-card/event-card.interfaces";

export const EventCardV1: FC<EventCardProps> = ({ event }: EventCardProps) => {
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
