import { CircularProgress, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { usePageLoadingIndicatorStyles } from "./page-loading-indicator.styles";

export const PageLoadingIndicator: FunctionComponent = () => {
    const pageLoadingIndicatorClasses = usePageLoadingIndicatorStyles();

    return (
        <Grid
            container
            alignItems="center"
            className={pageLoadingIndicatorClasses.container}
            justify="center"
        >
            <Grid item>
                {/* Loading indicator */}
                <CircularProgress color="primary" />
            </Grid>
        </Grid>
    );
};
