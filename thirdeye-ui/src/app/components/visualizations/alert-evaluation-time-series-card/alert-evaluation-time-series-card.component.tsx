import {
    Card,
    CardContent,
    CardHeader,
    FormHelperText,
    Grid,
    IconButton,
} from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";
import React, { FunctionComponent } from "react";
import { AlertEvaluationTimeSeries } from "../alert-evaluation-time-series/alert-evaluation-time-series/alert-evaluation-time-series.component";
import { VisualizationCard } from "../visualization-card/visualization-card.component";
import { AlertEvaluationTimeSeriesCardProps } from "./alert-evaluation-time-series-card.interfaces";
import { useAlertEvaluationTimeSeriesCardStyles } from "./alert-evaluation-time-series-card.styles";

export const AlertEvaluationTimeSeriesCard: FunctionComponent<AlertEvaluationTimeSeriesCardProps> = (
    props: AlertEvaluationTimeSeriesCardProps
) => {
    const alertEvaluationTimeSeriesCardClasses = useAlertEvaluationTimeSeriesCardStyles();

    return (
        <Card variant="outlined">
            <CardHeader
                action={
                    <Grid container alignItems="center" spacing={0}>
                        {/* Helper text */}
                        {props.helperText && (
                            <Grid item>
                                <FormHelperText
                                    className={
                                        alertEvaluationTimeSeriesCardClasses.helperText
                                    }
                                    error={props.error}
                                >
                                    {props.helperText}
                                </FormHelperText>
                            </Grid>
                        )}

                        {/* Refresh button */}
                        {!props.hideRefreshButton && (
                            <Grid item>
                                <IconButton onClick={props.onRefresh}>
                                    <RefreshIcon />
                                </IconButton>
                            </Grid>
                        )}
                    </Grid>
                }
                title={props.title}
                titleTypographyProps={{ variant: "h6" }}
            />

            <CardContent>
                <VisualizationCard
                    error={props.error}
                    helperText={props.helperText}
                    hideRefreshButton={props.hideRefreshButton}
                    title={props.maximizedTitle || props.title}
                    visualizationHeight={props.alertEvaluationTimeSeriesHeight}
                    visualizationMaximizedHeight={
                        props.alertEvaluationTimeSeriesMaximizedHeight
                    }
                    onRefresh={props.onRefresh}
                >
                    <AlertEvaluationTimeSeries
                        hideBrush
                        alertEvaluation={props.alertEvaluation}
                    />
                </VisualizationCard>
            </CardContent>
        </Card>
    );
};
