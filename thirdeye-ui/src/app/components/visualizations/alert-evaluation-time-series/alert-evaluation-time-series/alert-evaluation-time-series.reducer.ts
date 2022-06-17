///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

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
