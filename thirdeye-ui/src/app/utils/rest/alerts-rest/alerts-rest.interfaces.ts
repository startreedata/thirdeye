export interface Alert {
    id: number;
    name: string;
    description: string;

    lastTimestamp: number;
    active: boolean;

    subscriptionGroup: string[];
    metric: string;
    filters?: string | null;
    createdBy: string;
    updatedBy: string;
    datasetNames: string[];
    breakdownBy?: string;
    application: string[];
    rules: Array<{ detection: Detection[] }>;
    monitoringGranularity: string[];
}

export interface Detection {
    name: string;
    type: string;
    params: {
        [name: string]: string | number;
    };
}
