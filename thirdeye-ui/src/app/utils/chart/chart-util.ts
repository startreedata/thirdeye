import { GraphData } from "../../components/charts/line-graph.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";

export const getGraphDataFromAPIData = (data: AlertEvaluation): GraphData[] => {
    const lineChartData = data.detectionEvaluations.detection_rule_1.data;

    return lineChartData.timestamp.map((time, idx) => ({
        timestamp: new Date(time),
        current: lineChartData.current[idx],
        expacted: +lineChartData.expected[idx],
        lowerBound: +lineChartData.lowerBound[idx],
        upperBound: +lineChartData.upperBound[idx],
    }));
};
