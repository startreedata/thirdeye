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

export enum AnomalyBreakdownAPIOffsetValuesV2 {
    CURRENT = "current",
    ONE_WEEK_AGO = "P1W",
    TWO_WEEKS_AGO = "P2W",
    THREE_WEEKS_AGO = "P3W",
}
