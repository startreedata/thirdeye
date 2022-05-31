import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { EventRendererProps } from "./events-renderer.interfaces";

export const EventRenderer: FunctionComponent<EventRendererProps> = (
    props: EventRendererProps
) => {
    const { t } = useTranslation();

    const startTime =
        props.event && props.event.startTime
            ? formatDateAndTimeV1(props.event.startTime)
            : t("label.no-data-marker");
    const endTime =
        props.event && props.event.endTime
            ? formatDateAndTimeV1(props.event.endTime)
            : t("label.no-data-marker");

    return (
        <Grid container item justifyContent="flex-end">
            {/* Name */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.name")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    {(props.event && props.event.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Type */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.type")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    {(props.event && props.event.type) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Start Time */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.start-time")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">{startTime}</Typography>
            </Grid>

            {/* End Time */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.end-time")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">{endTime}</Typography>
            </Grid>
        </Grid>
    );
};
