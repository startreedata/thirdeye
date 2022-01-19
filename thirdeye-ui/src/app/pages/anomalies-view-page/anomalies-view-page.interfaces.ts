export interface AnomaliesViewPageParams {
    id: string;
}

export enum AnomalyBreakdownAPIOffsetValues {
    CURRENT = "current",
    ONE_WEEK_AGO = "wo1w",
    TWO_WEEKS_AGO = "wo2w",
    THREE_WEEKS_AGO = "wo3w",
}

export enum AnomalyBreakdownAPIOffsetValuesV2 {
    CURRENT = "current",
    ONE_WEEK_AGO = "P1W",
    TWO_WEEKS_AGO = "P2W",
    THREE_WEEKS_AGO = "P3W",
}
