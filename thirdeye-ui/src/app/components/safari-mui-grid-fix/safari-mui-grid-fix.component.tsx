import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useSafariMuiGridFixStyles } from "./safari-mui-grid-fix.styles";

// Safari has issues calculating width of components with top level Material-UI Grid container and
// always wraps the last grid item in the container
// https://github.com/mui-org/material-ui/issues/17142
// This grid item with strictly 1px footprint can be added to the container to fix the issue
export const SafariMuiGridFix: FunctionComponent = () => {
    const safariMuiGridFixClasses = useSafariMuiGridFixStyles();

    return <Grid item className={safariMuiGridFixClasses.safariMuiGridFix} />;
};
