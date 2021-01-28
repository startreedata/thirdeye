import {
    Card,
    CardContent,
    CardHeader,
    IconButton,
    Typography,
} from "@material-ui/core";
import { Refresh } from "@material-ui/icons";
import React, { FunctionComponent } from "react";
import { AlertEvaluationTimeSeries } from "../alert-evaluation-time-series/alert-evaluation-time-series.component";
import { AlertEvaluationTimeSeriesCardProps } from "./alert-evaluation-time-series-card.interfaces";
import { useAlertEvaluationTimeSeriesCardStyles } from "./alert-evaluation-time-series-card.styles";

export const AlertEvaluationTimeSeriesCard: FunctionComponent<AlertEvaluationTimeSeriesCardProps> = (
    props: AlertEvaluationTimeSeriesCardProps
) => {
    const alertEvaluationTimeSeriesCardClasses = useAlertEvaluationTimeSeriesCardStyles();

    return (
        <Card variant="outlined">
            {(props.title || props.showRefreshButton) && (
                <CardHeader
                    disableTypography
                    action={
                        <>
                            {/* Refresh button */}
                            {props.showRefreshButton && (
                                <IconButton onClick={props.onRefresh}>
                                    <Refresh />
                                </IconButton>
                            )}
                        </>
                    }
                    title={
                        <>
                            {/* Title */}
                            {props.title && (
                                <Typography variant="h6">
                                    {props.title}
                                </Typography>
                            )}
                        </>
                    }
                />
            )}

            <CardContent
                className={alertEvaluationTimeSeriesCardClasses.container}
            >
                <AlertEvaluationTimeSeries
                    alertEvaluation={props.alertEvaluation}
                />
            </CardContent>
        </Card>
    );
};
