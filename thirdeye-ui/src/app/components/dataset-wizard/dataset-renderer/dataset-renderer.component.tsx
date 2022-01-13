import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { DatasetRendererProps } from "./dataset-renderer.interfaces";

export const DatasetRenderer: FunctionComponent<DatasetRendererProps> = (
    props: DatasetRendererProps
) => {
    const { t } = useTranslation();

    return (
        <Grid container item justify="flex-end">
            {/* Name */}
            <Grid item sm={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.name")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={10}>
                <Typography variant="body2">
                    {(props.dataset && props.dataset.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Associated Datasource */}
            <Grid item sm={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.associated-datasource")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={10}>
                <Typography variant="body2">
                    {(props.dataset && props.dataset.dataSource?.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>
        </Grid>
    );
};
