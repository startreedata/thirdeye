import { CircularProgress } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useLoadingIndicatorStyles } from "./loading-indicator.styles";

export const LoadingIndicator: FunctionComponent = () => {
    const loadingIndicatorClasses = useLoadingIndicatorStyles();

    return (
        <div className={loadingIndicatorClasses.loadigIndicator}>
            {/* Loading indicator */}
            <CircularProgress color="primary" />
        </div>
    );
};
