import {
    Box,
    Button,
    Card,
    CardContent,
    CardHeader,
    FormHelperText,
    Grid,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { AlertEvaluationTimeSeries } from "../alert-evaluation-time-series/alert-evaluation-time-series/alert-evaluation-time-series.component";
import { VisualizationCard } from "../visualization-card/visualization-card.component";
import { AlertEvaluationTimeSeriesCardProps } from "./alert-evaluation-time-series-card.interfaces";
import { useAlertEvaluationTimeSeriesCardStyles } from "./alert-evaluation-time-series-card.styles";

export const AlertEvaluationTimeSeriesCard: FunctionComponent<
    AlertEvaluationTimeSeriesCardProps
> = (props: AlertEvaluationTimeSeriesCardProps) => {
    const alertEvaluationTimeSeriesCardClasses =
        useAlertEvaluationTimeSeriesCardStyles();

    if (props.isLoading) {
        return (
            <PageContentsCardV1>
                <SkeletonV1 animation="pulse" height={500} variant="rect" />
            </PageContentsCardV1>
        );
    }

    return (
        <Card variant="outlined">
            <CardHeader
                action={
                    <Grid container>
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

                        <Grid item>
                            <TimeRangeButtonWithContext
                                onTimeRangeChange={() =>
                                    props.onRefresh && props.onRefresh()
                                }
                            />
                        </Grid>

                        {/* Preview button */}
                        {props.showPreviewButton && (
                            <Grid item>
                                <Box>
                                    <Button
                                        color="primary"
                                        variant="contained"
                                        onClick={props.onRefresh}
                                    >
                                        Preview
                                    </Button>
                                </Box>
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
                        onAnomalyBarClick={props.onAnomalyBarClick}
                    />
                </VisualizationCard>
            </CardContent>
        </Card>
    );
};
