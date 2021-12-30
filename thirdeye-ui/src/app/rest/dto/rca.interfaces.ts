export interface AnomalyBreakdown {
    [key: string]: {
        [key: string]: number;
    };
}

export interface AnomalyBreakdownRequest {
    offset?: string;
    timezone?: string;
    filters?: string[];
    limit?: string;
}
