import { Datasource } from "./datasource.interfaces";

export interface Dataset {
    id: number;
    name: string;
    active: boolean;
    additive: boolean;
    dimensions: string[];
    timeColumn: TimeColumn;
    expectedDelay: Duration;
    dataSource: Datasource;
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
