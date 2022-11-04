export interface AppAnalytics {
    version: string;
    nMonitoredMetrics: number;
    anomalyStats: {
        totalCount: number;
        countWithFeedback: number;
        feedbackStats: {
            ANOMALY: number;
            NOT_ANOMALY: number;
            NO_FEEDBACK: number;
            ANOMALY_EXPECTED: number;
            ANOMALY_NEW_TREND: number;
        };
    };
}
