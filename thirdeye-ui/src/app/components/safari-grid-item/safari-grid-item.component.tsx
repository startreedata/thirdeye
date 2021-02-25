import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useSafariGridItemStyles } from "./safari-grid-item.styles";

// Safari has issues calculating width of components with top level Material-UI Grid container and
// always wraps the lst grid item in the said container
// https://github.com/mui-org/material-ui/issues/17142
// This grid item with strictly 1px footprint can be added to such components to fix the issue
export const SafariGridItem: FunctionComponent = () => {
    const safariGridItemClasses = useSafariGridItemStyles();

    return (
        <Grid
            item
            className={safariGridItemClasses.safariGridItem}
            spacing={0}
        />
    );
};
