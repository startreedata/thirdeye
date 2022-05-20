import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { EventRendererProps } from "./events-renderer.interfaces";

export const EventRenderer: FunctionComponent<EventRendererProps> = (
    props: EventRendererProps
) => {
    const { t } = useTranslation();

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
                    {(props.uiEvent && props.uiEvent.name) ||
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
                    {(props.uiEvent && props.uiEvent.type) ||
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
                <Typography variant="body2">
                    {(props.uiEvent && props.uiEvent.startTime) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* End Time */}
            <Grid item sm={3}>
                <Typography variant="subtitle1">
                    <strong>{t("label.end-time")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={9}>
                <Typography variant="body2">
                    {(props.uiEvent && props.uiEvent.endTime) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>
        </Grid>
    );
};
