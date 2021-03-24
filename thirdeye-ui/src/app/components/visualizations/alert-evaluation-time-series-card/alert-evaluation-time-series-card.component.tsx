import {
    Card,
    CardContent,
    CardHeader,
    FormHelperText,
    Grid,
    IconButton,
} from "@material-ui/core";
import FullscreenExitIcon from "@material-ui/icons/FullscreenExit";
import RefreshIcon from "@material-ui/icons/Refresh";
import React, { FunctionComponent, useEffect, useState } from "react";
import { AlertEvaluationTimeSeries } from "../alert-evaluation-time-series/alert-evaluation-time-series/alert-evaluation-time-series.component";
import { VisualizationCard } from "../visualization-card/visualization-card.component";
import { AlertEvaluationTimeSeriesCardProps } from "./alert-evaluation-time-series-card.interfaces";
import { useAlertEvaluationTimeSeriesCardStyles } from "./alert-evaluation-time-series-card.styles";

export const AlertEvaluationTimeSeriesCard: FunctionComponent<AlertEvaluationTimeSeriesCardProps> = (
    props: AlertEvaluationTimeSeriesCardProps
) => {
    const alertEvaluationTimeSeriesCardClasses = useAlertEvaluationTimeSeriesCardStyles();
    const [maximized, setMaximized] = useState(props.maximized);

    useEffect(() => {
        // Maximize/restore input changed, update
        setMaximized(props.maximized);
    }, [props.maximized]);

    const handleAlertEvaluationTimeSeriesCardMaximize = (): void => {
        setMaximized(true);
    };

    const handleAlertEvaluationTimeSeriesCardRestore = (): void => {
        setMaximized(false);
    };

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

                        {/* Maximize button */}
                        <Grid item>
                            <IconButton
                                onClick={
                                    handleAlertEvaluationTimeSeriesCardMaximize
                                }
                            >
                                <FullscreenExitIcon />
                            </IconButton>
                        </Grid>
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
                    maximized={maximized}
                    title={props.maximizedTitle || props.title}
                    visualizationHeight={props.alertEvaluationTimeSeriesHeight}
                    visualizationMaximizedHeight={
                        props.alertEvaluationTimeSeriesMaximizedHeight
                    }
                    onRefresh={props.onRefresh}
                    onRestore={handleAlertEvaluationTimeSeriesCardRestore}
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
