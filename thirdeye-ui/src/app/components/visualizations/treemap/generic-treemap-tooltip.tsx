import { Grid, Typography } from "@material-ui/core";
import React from "react";
import { formatLargeNumber } from "../../../utils/number/number.util";
import { SafariMuiGridFix } from "../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { useAlertEvaluationTimeSeriesTooltipStyles } from "../alert-evaluation-time-series/alert-evaluation-time-series-tooltip/alert-evaluation-time-series-tooltip.styles";
import { TreemapData } from "./treemap.interfaces";

function GenericTreemapTooltip<Data>(props: TreemapData<Data>): JSX.Element {
    const alertEvaluationTimeSeriesTooltipClasses = useAlertEvaluationTimeSeriesTooltipStyles();

    return (
        <Grid
            container
            className={
                alertEvaluationTimeSeriesTooltipClasses.alertEvaluationTimeSeriesTooltip
            }
            direction="column"
            spacing={0}
        >
            {/* Name of the Dimension */}
            <Grid item className={alertEvaluationTimeSeriesTooltipClasses.time}>
                <Grid
                    container
                    alignItems="center"
                    justify="center"
                    spacing={0}
                >
                    <Grid item>
                        <Typography variant="overline">{props.id}</Typography>
                    </Grid>
                </Grid>
            </Grid>

            <Grid
                item
                className={
                    alertEvaluationTimeSeriesTooltipClasses.nameValueContents
                }
            >
                {/* Value */}
                <Typography variant="overline">
                    {formatLargeNumber(props.size)}
                </Typography>
            </Grid>

            <SafariMuiGridFix />
        </Grid>
    );
}

export { GenericTreemapTooltip };
