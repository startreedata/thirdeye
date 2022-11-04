export interface NewAlertTemplate {
    name: string;
    description: string;
    cron?: string;
    nodes?: AlertTemplateNode[];

    rca?: {
        datasource: string;
        dataset: string;
        metric: string;
    };

    metadata?: {
        datasource: {
            name: string;
        };
        dataset: {
            name: string;
        };
        metric: {
            name: string;
        };
    };

    defaultProperties?: { [index: string]: string };
}

export interface AlertTemplate extends NewAlertTemplate {
    id: number;
}

export interface AlertTemplateNode {
    name: string;
    type: string;
    params: { [index: string]: unknown };
    inputs?: AlertTemplateNodeInput[];
    outputs?: AlertTemplateNodeOutput[];
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
