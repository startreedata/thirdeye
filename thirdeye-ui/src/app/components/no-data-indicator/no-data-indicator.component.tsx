import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { NoDataIndicatorProps } from "./no-data-indicator.interfaces";
import { useNoDataIndicatorStyles } from "./no-data-indicator.styles";

export const NoDataIndicator: FunctionComponent<NoDataIndicatorProps> = (
    props: NoDataIndicatorProps
) => {
    const noDataIndicatorClasses = useNoDataIndicatorStyles();
    const { t } = useTranslation();

    return (
        <Grid
            container
            alignItems="center"
            className={noDataIndicatorClasses.container}
            justify="center"
        >
            {/* No data available message */}
            <Grid item>
                <Typography variant="body1">
                    {props.text ? props.text : t("message.no-data-available")}
                </Typography>
            </Grid>
        </Grid>
    );
};
