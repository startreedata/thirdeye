import produce from "immer";
import {
    AlertEvaluationTimeSeriesState,
    AlertEvaluationTimeSeriesStateAction,
} from "./alert-evaluation-time-series.interfaces";

export const alertEvaluationTimeSeriesReducer = (
    state: AlertEvaluationTimeSeriesState,
    action: {
        type: AlertEvaluationTimeSeriesStateAction;
        payload?: Partial<AlertEvaluationTimeSeriesState>;
    }
): AlertEvaluationTimeSeriesState => {
    return produce(state, (draft) => {
        switch (action && action.type) {
            case AlertEvaluationTimeSeriesStateAction.UPDATE:
                draft = { ...draft, ...action.payload };

                return draft;
            case AlertEvaluationTimeSeriesStateAction.TOGGLE_CURRENT_PLOT_VISIBLE:
                draft.currentPlotVisible = !draft.currentPlotVisible;

                return;
            case AlertEvaluationTimeSeriesStateAction.TOGGLE_BASELINE_PLOT_VISIBLE:
                draft.baselinePlotVisible = !draft.baselinePlotVisible;

                return;
            case AlertEvaluationTimeSeriesStateAction.TOGGLE_UPPER_AND_LOWER_BOUND_PLOT_VISIBLE:
                draft.upperAndLowerBoundPlotVisible =
                    !draft.upperAndLowerBoundPlotVisible;

                return;
            case AlertEvaluationTimeSeriesStateAction.TOGGLE_ANOMALIES_PLOT_VISIBLE:
                draft.anomaliesPlotVisible = !draft.anomaliesPlotVisible;

                return;
        }
    });
};
