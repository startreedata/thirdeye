import { Grid, Typography } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { DatasetRendererProps } from "./dataset-renderer.interfaces";

export const DatasetRenderer: FunctionComponent<DatasetRendererProps> = (
    props: DatasetRendererProps
) => {
    const { t } = useTranslation();

    return (
        <Grid container justify="flex-end">
            {/* Name */}
            <Grid item sm={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.name")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={10}>
                <Typography variant="body1">
                    {(props.dataset && props.dataset.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Associated Datasources */}
            <Grid item sm={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.associated-datasources")}</strong>
                </Typography>
            </Grid>

            {/* No associated datasources */}
            {!props.dataset ||
                (isEmpty(props.dataset.datasources) && (
                    <Grid item sm={10}>
                        <Typography variant="body1">
                            {t("label.no-data-marker")}
                        </Typography>
                    </Grid>
                ))}

            {/* All associated datasources */}
            {props.dataset && !isEmpty(props.dataset.datasources) && (
                <Grid item sm={10}>
                    {props.dataset.datasources.map((datasource, index) => (
                        <Typography key={index} variant="body1">
                            {datasource.name}
                        </Typography>
                    ))}
                </Grid>
            )}
        </Grid>
    );
};
