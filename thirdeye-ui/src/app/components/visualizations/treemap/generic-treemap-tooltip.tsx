import { Grid, Typography } from "@material-ui/core";
import React from "react";
import { formatLargeNumberV1 } from "../../../platform/utils";
import { SafariMuiGridFix } from "../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { TreemapData } from "./treemap.interfaces";
import { useTreemapStyles } from "./treemap.styles";

function GenericTreemapTooltip<Data>(props: TreemapData<Data>): JSX.Element {
    const treemapStyles = useTreemapStyles();

    return (
        <Grid
            container
            className={treemapStyles.alertEvaluationTimeSeriesTooltip}
            direction="column"
            spacing={0}
        >
            {/* Name of the Dimension */}
            <Grid item className={treemapStyles.time}>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="center"
                    spacing={0}
                >
                    <Grid item>
                        <Typography variant="overline">{props.id}</Typography>
                    </Grid>
                </Grid>
            </Grid>

            <Grid item className={treemapStyles.nameValueContents}>
                {/* Value */}
                <Typography variant="overline">
                    {formatLargeNumberV1(props.size)}
                </Typography>
            </Grid>

            <SafariMuiGridFix />
        </Grid>
    );
}

export { GenericTreemapTooltip };
