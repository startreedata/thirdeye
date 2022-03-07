import { NewAlertTemplate } from "../../rest/dto/alert-template.interfaces";

export const createDefaultAlertTemplate = (): NewAlertTemplate => {
    return {
        name: "simple-threshold-template",
        description:
            "Sample threshold alert. Runs every hour. Change the template properties to run on your data",
        cron: "0 0 0 1/1 * ? *",
        nodes: [
            {
                name: "root",
                type: "AnomalyDetector",
                params: {
                    type: "THRESHOLD",
                    "component.timezone": "UTC",
                    "component.monitoringGranularity":
                        "${monitoringGranularity}",
                    "component.timestamp": "ts",
                    "component.metric": "met",
                    "component.max": "${max}",
                    "component.min": "${min}",
                    "anomaly.metric": "${aggregateFunction}(${metric})",
                },
                inputs: [
                    {
                        targetProperty: "current",
                        sourcePlanNode: "currentDataFetcher",
                        sourceProperty: "currentData",
                    },
                ],
            },
            {
                name: "currentDataFetcher",
                type: "DataFetcher",
                params: {
                    "component.dataSource": "${dataSource}",
                    "component.query":
                        "SELECT __timeGroup(\"${timeColumn}\", '${timeColumnFormat}', '${monitoringGranularity}') " +
                        "as ts, ${aggregateFunction}(${metric}) as met FROM ${dataset} WHERE __timeFilter(ts) GROUP BY" +
                        " ts ORDER BY ts LIMIT 1000",
                },
                outputs: [
                    {
                        outputKey: "pinot",
                        outputName: "currentData",
                    },
                ],
            },
        ],
        metadata: {
            datasource: {
                name: "${dataSource}",
            },
            dataset: {
                name: "${dataset}",
            },
            metric: {
                name: "${metric}",
            },
        },
    };
};
