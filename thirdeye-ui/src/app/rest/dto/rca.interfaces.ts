import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";

export interface AnomalyBreakdown {
    metric: {
        name: string;
        dataset: {
            name: string;
        };
    };
    current: {
        breakdown: {
            [key: string]: {
                [key: string]: number;
            };
        };
    };
    baseline: {
        breakdown: {
            [key: string]: {
                [key: string]: number;
            };
        };
    };
}

export interface AnomalyBreakdownRequest {
    baselineOffset?: AnomalyBreakdownAPIOffsetValues;
    timezone?: string;
    filters?: string[];
    limit?: number;
}
