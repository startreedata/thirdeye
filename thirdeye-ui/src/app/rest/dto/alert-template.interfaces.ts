import { AlertNodeType } from "./alert.interfaces";

export interface AlertTemplate {
    id: number;
    name: string;
    description: string;
    cron: string;
    nodes: AlertTemplateNode[];

    rca: {
        datasource: string;
        dataset: string;
        metric: string;
    };
}

export interface AlertTemplateNode {
    name: string;
    type: AlertNodeType;
    params: { [index: string]: unknown };
    inputs: AlertTemplateNodeInput[];
    outputs: AlertTemplateNodeOutput[];
}

export interface AlertTemplateNodeInput {
    targetProperty: string;
    sourcePlanNode: string;
    sourceProperty: string;
}

export interface AlertTemplateNodeOutput {
    outputKey: string;
    outputName: string;
}
