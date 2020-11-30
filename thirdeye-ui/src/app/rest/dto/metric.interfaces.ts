export interface Metric {
    id: number;
    name: string;
    urn: string;
    dataset: Dataset;
    active: boolean;
    created: number;
    updated: number;
}

export interface Dataset {
    id: number;
    name: string;
    active: boolean;
    additive: boolean;
    dimensions: string[];
    timeColumn: TimeColumn;
    expectedDelay: Duration;
}

export interface TimeColumn {
    name: string;
    interval: Duration;
    format: string;
    timezone: string;
}

export interface Duration {
    seconds: number;
    units: TemporalUnit[];
    nano: number;
    negative: boolean;
    zero: boolean;
}

export interface TemporalUnit {
    numberBased: boolean;
    timeBased: boolean;
    duration: Duration;
    durationEstimated: boolean;
}
