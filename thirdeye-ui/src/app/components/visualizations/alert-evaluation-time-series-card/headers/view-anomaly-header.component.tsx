import { CardContent, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { DEFAULT_FEEDBACK } from "../../../../utils/alerts/alerts.util";
import { AnomalyFeedback } from "../../../anomaly-feedback/anomaly-feedback.component";
import { TimeRangeButtonWithContext } from "../../../time-range/time-range-button-with-context/time-range-button.component";
import { ViewAnomalyHeaderProps } from "../alert-evaluation-time-series-card.interfaces";

export const ViewAnomalyHeader: FunctionComponent<ViewAnomalyHeaderProps> = ({
    anomaly,
    onRefresh,
}) => {
    return (
        <CardContent>
            <Grid container justifyContent="space-between">
                <Grid item lg="auto" md="auto" sm={4} xs={12}>
                    {anomaly && (
                        <AnomalyFeedback
                            anomalyFeedback={
                                anomaly.feedback || {
                                    ...DEFAULT_FEEDBACK,
                                }
                            }
                            anomalyId={anomaly.id}
                        />
                    )}
                </Grid>

                <Grid
                    item
                    alignContent="flex-end"
                    lg="auto"
                    md="auto"
                    sm={8}
                    xs={12}
                >
                    <TimeRangeButtonWithContext
                        onTimeRangeChange={(start: number, end: number) =>
                            onRefresh && onRefresh(start, end)
                        }
                    />
                </Grid>
            </Grid>
        </CardContent>
    );
};
