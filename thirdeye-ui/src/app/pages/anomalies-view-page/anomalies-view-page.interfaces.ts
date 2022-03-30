export type AnomaliesViewPageParams = {
    id: string;
};

export enum AnomalyBreakdownAPIOffsetValues {
    CURRENT = "current",
    ONE_WEEK_AGO = "P1W",
    TWO_WEEKS_AGO = "P2W",
    THREE_WEEKS_AGO = "P3W",
    FOUR_WEEKS_AGO = "P4W",
}

export const OFFSET_TO_HUMAN_READABLE = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: "",
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: "One Week Ago",
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: "Two Weeks Ago",
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: "Three Weeks Ago",
    [AnomalyBreakdownAPIOffsetValues.FOUR_WEEKS_AGO]: "Four Weeks Ago",
};

export const BASELINE_OPTIONS: {
    key: AnomalyBreakdownAPIOffsetValues;
    description: string;
}[] = [];

Object.values(AnomalyBreakdownAPIOffsetValues).forEach(
    (offsetKey: AnomalyBreakdownAPIOffsetValues) => {
        if (offsetKey !== AnomalyBreakdownAPIOffsetValues.CURRENT) {
            BASELINE_OPTIONS.push({
                key: offsetKey,
                description: OFFSET_TO_HUMAN_READABLE[offsetKey],
            });
        }
    }
);
