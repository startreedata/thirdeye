import { CircularProgress, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useLoadingIndicatorStyles } from "./loading-indicator.styles";

export const LoadingIndicator: FunctionComponent = () => {
    const loadingIndicatorClasses = useLoadingIndicatorStyles();

    return (
        <Grid
            container
            alignItems="center"
            className={loadingIndicatorClasses.container}
            justify="center"
        >
            <Grid item>
                {/* Loading indicator */}
                <CircularProgress color="primary" />
            </Grid>
        </Grid>
    );
};
