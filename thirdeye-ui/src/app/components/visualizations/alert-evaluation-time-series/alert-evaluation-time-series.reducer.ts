import produce from "immer";
import {
    AlertEvaluationTimeSeriesInternalState,
    AlertEvaluationTimeSeriesInternalStateAction,
} from "./alert-evaluation-time-series.interfaces";

export const alertEvaluationTimeSeriesInternalReducer = (
    state: AlertEvaluationTimeSeriesInternalState,
    action: {
        type: AlertEvaluationTimeSeriesInternalStateAction;
        payload?: Partial<AlertEvaluationTimeSeriesInternalState>;
    }
): AlertEvaluationTimeSeriesInternalState => {
    return produce(state, (draft) => {
        switch (action && action.type) {
            case AlertEvaluationTimeSeriesInternalStateAction.UPDATE:
                draft = { ...draft, ...action.payload };

                return draft;
            case AlertEvaluationTimeSeriesInternalStateAction.TOGGLE_CURRENT_PLOT_VISIBLE:
                draft.currentPlotVisible = !draft.currentPlotVisible;

                return;
            case AlertEvaluationTimeSeriesInternalStateAction.TOGGLE_BASELINE_PLOT_VISIBLE:
                draft.baselinePlotVisible = !draft.baselinePlotVisible;

                return;
            case AlertEvaluationTimeSeriesInternalStateAction.TOGGLE_UPPER_AND_LOWER_BOUND_PLOT_VISIBLE:
                draft.upperAndLowerBoundPlotVisible = !draft.upperAndLowerBoundPlotVisible;

                return;
            case AlertEvaluationTimeSeriesInternalStateAction.TOGGLE_ANOMALIES_PLOT_VISIBLE:
                draft.anomaliesPlotVisible = !draft.anomaliesPlotVisible;

                return;
        }
    });
};
