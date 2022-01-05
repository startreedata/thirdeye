import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";

export interface AnomalyBreakdown {
    [key: string]: {
        [key: string]: number;
    };
}

export interface AnomalyBreakdownRequest {
    offset?: AnomalyBreakdownAPIOffsetValues;
    timezone?: string;
    filters?: string[];
    limit?: number;
}
