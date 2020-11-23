import { Grid, Typography, useTheme } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as EmptyGlass } from "../../../assets/icons/empty-glass.svg";
import { usePageNotFoundIndicatorStyles } from "./page-not-found-indicator.styles";

export const PageNotFoundIndicator: FunctionComponent = () => {
    const pageNotFoundIndicatorClasses = usePageNotFoundIndicatorStyles();
    const theme = useTheme();
    const { t } = useTranslation();

    return (
        <Grid container alignItems="center" direction="column">
            <Grid item>
                {/* Page not found icon */}
                <EmptyGlass fill={theme.palette.primary.main} height={100} />
            </Grid>

            <Grid item>
                {/* Page not found error code */}
                <Typography
                    className={pageNotFoundIndicatorClasses.errorCode}
                    color="primary"
                    variant="h1"
                >
                    {t("label.404")}
                </Typography>
            </Grid>

            <Grid item>
                {/* Page not found error message */}
                <Typography variant="subtitle1">
                    {t("message.page-not-found")}
                </Typography>
            </Grid>
        </Grid>
    );
};
