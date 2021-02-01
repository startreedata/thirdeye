export interface Metric {
    id: number;
    name: string;
    urn: string;
    dataset: Dataset;
    active: boolean;
    created: number;
    updated: number;
    derived: boolean;
    derivedMetricExpression: string;
    aggregationColumn: string;
    aggregationFunction: MetricAggFunction;
    rollupThreshold: number;
    views: LogicalView[];
    where: string;
}

export interface Dataset {
    id: number;
    name: string;
    active: boolean;
    additive: boolean;
    dimensions: string[];
    timeColumn: TimeColumn;
    expectedDelay: Duration;
    dataSource: string;
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
    zero: boolean;
    negative: boolean;
}

export interface TemporalUnit {
    timeBased: boolean;
    numberBased: boolean;
    duration: Duration;
    durationEstimated: boolean;
}

export interface LogicalView {
    name: string;
    query: string;
}

export enum MetricAggFunction {
    SUM = "SUM",
    AVG = "AVG",
    COUNT = "COUNT",
    COUNT_DISTINCT = "COUNT_DISTINCT",
    MAX = "MAX",
    PCT50 = "PCT50",
    PCT90 = "PCT90",
    PCT95 = "PCT95",
    PCT99 = "PCT99",
}
