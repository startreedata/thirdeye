export interface Alert {
    id: number;
    name: string;
    description: string;
    lastTimestamp: number;
    active: boolean;
    owner: Owner;
    created: number;
    updated: number;
    detections: { [name: string]: Detection };
    filters: { [name: string]: Detection };
    qualityChecks: { [name: string]: Detection };
}

export interface Detection {
    name: string;
    type: string;
    metric: Metric;
    params: {
        [name: string]: string | number;
    };
    alert: Alert;
}

export interface Metric {
    id: number;
    urn: string;
    dataset: Dataset;
    name: string;
    active: boolean;
    created: number;
    updated: number;
}

export interface Dataset {
    id: number;
    name: string;
    active: boolean;
    dimensions: string[];
}

export interface Owner {
    principal: string;
}
