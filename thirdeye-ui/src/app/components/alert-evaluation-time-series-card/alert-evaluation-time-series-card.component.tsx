import { Card, CardContent } from "@material-ui/core";
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
