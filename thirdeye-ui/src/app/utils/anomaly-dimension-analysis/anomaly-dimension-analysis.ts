import {
    AnomalyDimensionAnalysisData,
    AnomalyDimensionAnalysisMetricRow,
} from "../../rest/dto/rca.interfaces";

export const RCA_NO_FILTER_MARKER = "(NO_FILTER)";

export const getFilterDimensionAnalysisData = (
    data: AnomalyDimensionAnalysisData
): AnomalyDimensionAnalysisData => {
    return {
        ...data,
        responseRows: data.responseRows.map(
            (responseRow: AnomalyDimensionAnalysisMetricRow) => ({
                ...responseRow,
                names: responseRow.names.filter(
                    (name) => name !== RCA_NO_FILTER_MARKER
                ),
            })
        ),
    };
};
