import React, { FunctionComponent } from "react";
import { AlertEvaluationTimeSeries } from "../alert-evaluation-time-series/alert-evaluation-time-series.component";
import { VisualizationCard } from "../visualization-card/visualization-card.component";
import { AlertEvaluationTimeSeriesCardProps } from "./alert-evaluation-time-series-card.interfaces";

export const AlertEvaluationTimeSeriesCard: FunctionComponent<AlertEvaluationTimeSeriesCardProps> = (
    props: AlertEvaluationTimeSeriesCardProps
) => {
    return (
        <VisualizationCard {...props}>
            <AlertEvaluationTimeSeries
                alertEvaluation={props.alertEvaluation}
            />
        </VisualizationCard>
    );
};
