import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNoDataAvailableIndicatorStyles } from "./no-data-available-indicator.styles";

export const NoDataAvailableIndicator: FunctionComponent = () => {
    const noDataAvailableIndicatorClasses = useNoDataAvailableIndicatorStyles();
    const { t } = useTranslation();

    return (
        <Grid
            container
            alignItems="center"
            className={noDataAvailableIndicatorClasses.container}
            justify="center"
        >
            <Grid item>
                {/* No data available message */}
                {t("message.no-data-available")}
            </Grid>
        </Grid>
    );
};
